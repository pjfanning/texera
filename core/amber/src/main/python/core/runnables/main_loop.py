import threading
import traceback
import typing
from queue import Queue
from typing import Iterator, List, MutableMapping, Optional, Union

import pyarrow
from loguru import logger
from overrides import overrides
from pampy import match
from pandas._libs.missing import checknull
from pyarrow.util import find_free_port

from core.architecture.managers.context import Context
from core.architecture.managers.pause_manager import PauseType
from core.architecture.packaging.batch_to_tuple_converter import EndOfAllMarker
from core.architecture.rpc.async_rpc_client import AsyncRPCClient
from core.architecture.rpc.async_rpc_server import AsyncRPCServer
from core.models import (
    ControlElement,
    DataElement,
    InputExhausted,
    InternalQueue,
    Operator,
    SenderChangeMarker,
    Tuple,
)
from core.runnables.data_processor import DataProcessor
from core.util import IQueue, StoppableQueueBlockingRunnable, get_one_of, set_one_of, \
    DoubleBlockingQueue
from core.util.operator import Option
from core.util.print_writer.print_log_handler import PrintLogHandler
from proto.edu.uci.ics.amber.engine.architecture.worker import (
    ControlCommandV2,
    LocalOperatorExceptionV2,
    WorkerExecutionCompletedV2,
    WorkerState,
    LinkCompletedV2,
    PythonConsoleMessageV2,
)
from proto.edu.uci.ics.amber.engine.common import (
    ActorVirtualIdentity,
    ControlInvocationV2,
    ControlPayloadV2,
    LinkIdentity,
    ReturnInvocationV2,
)


class MainLoop(StoppableQueueBlockingRunnable):
    def __init__(self, input_queue: InternalQueue, output_queue: InternalQueue):
        super().__init__(self.__class__.__name__, queue=input_queue)

        self._input_queue: InternalQueue = input_queue
        self._output_queue: InternalQueue = output_queue
        self._operator: Optional[Operator] = Option()
        self._current_input_tuple: Optional[Union[Tuple, InputExhausted]] = None
        self._current_input_link: Optional[LinkIdentity] = None
        self._current_input_tuple_iter: Optional[
            Iterator[Union[Tuple, InputExhausted]]
        ] = None
        self._input_links: List[LinkIdentity] = list()
        self._input_link_map: MutableMapping[LinkIdentity, int] = dict()

        self.context = Context(self)
        self._async_rpc_server = AsyncRPCServer(output_queue, context=self.context)
        self._async_rpc_client = AsyncRPCClient(output_queue, context=self.context)
        import datetime

        self._print_log_handler = PrintLogHandler(
            lambda msg: self._async_rpc_client.send(
                ActorVirtualIdentity(name="CONTROLLER"),
                set_one_of(
                    ControlCommandV2,
                    PythonConsoleMessageV2(
                        timestamp=datetime.datetime.now(), level="PRINT", message=msg
                    ),
                ),
            )
        )

        self._data_input_queue = DoubleBlockingQueue(tuple)
        self._data_output_queue = Queue()
        self._dp_process_condition = threading.Condition()
        self.data_processor = DataProcessor(
            self._data_input_queue,
            self._data_output_queue,
            self._dp_process_condition,
            self.context,
        )

        self._tdb_port = find_free_port()

        threading.Thread(target=self.data_processor.run, daemon=True).start()

    def complete(self) -> None:
        """
        Complete the DataProcessor, marking state to COMPLETED, and notify the
        controller.
        """
        # flush the buffered console prints
        self._print_log_handler.flush()
        self._operator.get().close()
        # stop the data processing thread
        self.data_processor.stop()
        self.context.state_manager.transit_to(WorkerState.COMPLETED)
        control_command = set_one_of(ControlCommandV2, WorkerExecutionCompletedV2())
        self._async_rpc_client.send(
            ActorVirtualIdentity(name="CONTROLLER"), control_command
        )

    def check_and_process_control(self) -> None:
        """
        Check if there exists any ControlElement(s) in the input_queue, if so, take and
        process them one by one.

        This is used very frequently as we want to prioritize the process of
        ControlElement, and will be invoked many times during a DataElement's processing
        lifecycle. Thus, this method's invocation could appear in any stage while
        processing a DataElement.
        """
        self.check_pdb()
        while (
            not self._input_queue.main_empty() or self.context.pause_manager.is_paused()
        ):
            next_entry = self.interruptible_get()
            self._process_control_element(next_entry)
        self.check_pdb()
    @overrides
    def pre_start(self) -> None:
        self.context.state_manager.assert_state(WorkerState.UNINITIALIZED)
        self.context.state_manager.transit_to(WorkerState.READY)

    @overrides
    def receive(self, next_entry: IQueue.QueueElement) -> None:
        """
        Main entry point of the DataProcessor. Upon receipt of an next_entry,
        process it respectfully.

        :param next_entry: An entry from input_queue, could be one of the followings:
                    1. a ControlElement;
                    2. a DataElement.
        """
        match(
            next_entry,
            DataElement,
            self._process_data_element,
            ControlElement,
            self._process_control_element,
        )

    def process_control_payload(
        self, tag: ActorVirtualIdentity, payload: ControlPayloadV2
    ) -> None:
        """
        Process the given ControlPayload with the tag.

        :param tag: ActorVirtualIdentity, the sender.
        :param payload: ControlPayloadV2 to be handled.
        """
        # logger.debug(f"processing one CONTROL: {payload} from {tag}")
        match(
            (tag, get_one_of(payload)),
            typing.Tuple[ActorVirtualIdentity, ControlInvocationV2],
            self._async_rpc_server.receive,
            typing.Tuple[ActorVirtualIdentity, ReturnInvocationV2],
            self._async_rpc_client.receive,
        )

    def process_input_tuple(self) -> None:
        """
        Process the current input tuple with the current input link. Send all result
        Tuples to downstream operators.

        This is being invoked for each Tuple/Marker that are unpacked from the
        DataElement.
        """
        if isinstance(self._current_input_tuple, Tuple):
            self.context.statistics_manager.increase_input_tuple_count()
        try:
            for output_tuple in self.process_tuple_with_udf(
                self._current_input_tuple, self._current_input_link
            ):
                self.check_and_process_control()
                if output_tuple is not None:
                    schema = self._operator.get().output_schema
                    self.cast_tuple_to_match_schema(output_tuple, schema)
                    self.context.statistics_manager.increase_output_tuple_count()
                    for (
                        to,
                        batch,
                    ) in self.context.tuple_to_batch_converter.tuple_to_batch(
                        output_tuple
                    ):
                        batch.schema = self._operator.get().output_schema
                        self._output_queue.put(DataElement(tag=to, payload=batch))
        except Exception as err:
            logger.exception(err)
            self.report_exception()
            self._pause()

    def process_tuple_with_udf(
        self, tuple_: Union[Tuple, InputExhausted], link: LinkIdentity
    ) -> Iterator[Optional[Tuple]]:
        """
        Process the Tuple/InputExhausted with the current link.

        This is a wrapper to invoke processing of the operator.

        :param tuple_: Union[Tuple, InputExhausted], the current tuple.
        :param link: LinkIdentity, the current link.
        :return: Iterator[Tuple], iterator of result Tuple(s).
        """

        # bind link with input index
        if link not in self._input_link_map:
            self._input_links.append(link)
            index = len(self._input_links) - 1
            self._input_link_map[link] = index
        input_ = self._input_link_map[link]

        self._data_input_queue.put((tuple_, input_))
        print("put data into data input queue", tuple_)
        self.data_processor._finished_current.clear()
        self._context_switch()

        while not self.data_processor._finished_current.is_set():
            self.check_and_process_control()

            num_outputs = self._data_output_queue.qsize()
            if num_outputs:
                for _ in range(num_outputs):
                    self.check_and_process_control()
                    yield self._data_output_queue.get()

            self._context_switch()

    def report_exception(self) -> None:
        """
        Report the traceback of current stack when an exception occurs.
        """
        self._print_log_handler.flush()
        message: str = traceback.format_exc(limit=-1)
        control_command = set_one_of(
            ControlCommandV2, LocalOperatorExceptionV2(message=message)
        )
        self._async_rpc_client.send(
            ActorVirtualIdentity(name="CONTROLLER"), control_command
        )

    def _process_control_element(self, control_element: ControlElement) -> None:
        """
        Upon receipt of a ControlElement, unpack it into tag and payload to be handled.

        :param control_element: ControlElement to be handled.
        """
        print(control_element)
        self.process_control_payload(control_element.tag, control_element.payload)

    def _process_tuple(self, tuple_: Union[Tuple, InputExhausted]) -> None:
        self._current_input_tuple = tuple_
        self.process_input_tuple()
        self.check_and_process_control()

    def _process_input_exhausted(self, input_exhausted: InputExhausted):
        self._process_tuple(input_exhausted)
        if self._current_input_link is not None:
            control_command = set_one_of(
                ControlCommandV2, LinkCompletedV2(self._current_input_link)
            )
            self._async_rpc_client.send(
                ActorVirtualIdentity(name="CONTROLLER"), control_command
            )

    def _process_sender_change_marker(
        self, sender_change_marker: SenderChangeMarker
    ) -> None:
        """
        Upon receipt of a SenderChangeMarker, change the current input link to the
        sender.

        :param sender_change_marker: SenderChangeMarker which contains sender link.
        """
        self._current_input_link = sender_change_marker.link

    def _process_end_of_all_marker(self, _: EndOfAllMarker) -> None:
        """
        Upon receipt of an EndOfAllMarker, which indicates the end of all input links,
        send the last data batches to all downstream workers.

        It will also invoke complete() of this DataProcessor.

        :param _: EndOfAllMarker
        """
        for to, batch in self.context.tuple_to_batch_converter.emit_end_of_upstream():
            batch.schema = self._operator.get().output_schema
            self._output_queue.put(DataElement(tag=to, payload=batch))
            self.check_and_process_control()
        self.complete()

    def _process_data_element(self, data_element: DataElement) -> None:
        """
        Upon receipt of a DataElement, unpack it into Tuples and Markers,
        and process them one by one.

        :param data_element: DataElement, a batch of data.
        """
        # Update state to RUNNING
        if self.context.state_manager.confirm_state(WorkerState.READY):
            self.context.state_manager.transit_to(WorkerState.RUNNING)

        self._current_input_tuple_iter = (
            self.context.batch_to_tuple_converter.process_data_payload(
                data_element.tag, data_element.payload
            )
        )

        if self._current_input_tuple_iter is None:
            return
        # here the self._current_input_tuple_iter could be modified during iteration,
        # thus we are using the try-while-stop_iteration way to iterate through the
        # iterator, instead of the for-each-loop syntax sugar.
        while True:
            # In Python@3.8 there is a new `:=` operator to simplify this assignment
            # in while-loop. For now we keep it this way to support versions below
            # 3.8.
            try:
                element = next(self._current_input_tuple_iter)
            except StopIteration:
                # StopIteration is the standard way for an iterator to end, we handle
                # it and terminate the loop.
                break
            try:
                match(
                    element,
                    Tuple,
                    self._process_tuple,
                    InputExhausted,
                    self._process_input_exhausted,
                    SenderChangeMarker,
                    self._process_sender_change_marker,
                    EndOfAllMarker,
                    self._process_end_of_all_marker,
                )
            except Exception as err:
                logger.exception(err)

    def _scheduler_time_slot_event(self, time_slot_expired: bool) -> None:
        """
        The time slot for scheduling this worker has expired.
        """
        if time_slot_expired:
            self.context.pause_manager.record_request(
                PauseType.SCHEDULER_TIME_SLOT_EXPIRED_PAUSE, True
            )
            self._input_queue.disable_sub()
        else:
            self.context.pause_manager.record_request(
                PauseType.SCHEDULER_TIME_SLOT_EXPIRED_PAUSE, False
            )
            if not self.context.pause_manager.is_paused():
                self.context.input_queue.enable_sub()

    def _pause(self) -> None:
        """
        Pause the data processing.
        """
        self._print_log_handler.flush()
        if self.context.state_manager.confirm_state(
            WorkerState.RUNNING, WorkerState.READY
        ):
            self.context.pause_manager.record_request(PauseType.USER_PAUSE, True)
            self.context.state_manager.transit_to(WorkerState.PAUSED)
            self._input_queue.disable_sub()
            if self.data_processor.notifiable.is_set():
                self._data_input_queue.put("set breakpoint")
                self._context_switch()

    def _resume(self, mode = 'c') -> None:
        """
        Resume the data processing.
        """
        if self.context.state_manager.confirm_state(WorkerState.PAUSED):
            self.context.pause_manager.record_request(PauseType.USER_PAUSE, False)
            if not self.context.pause_manager.is_paused():
                if not self.data_processor.notifiable.is_set():
                    logger.debug(f"sending command to pdb [{mode}]")
                    self.data_processor.notifiable.set()
                    self.data_processor.debug_input_queue.put(f"{mode}\n")
                self.context.input_queue.enable_sub()
            self.context.state_manager.transit_to(WorkerState.RUNNING)
            print("resumed")

    @staticmethod
    def cast_tuple_to_match_schema(output_tuple, schema):
        # TODO: move this into Tuple, after making Tuple aware of Schema

        # right now only support casting ANY to binary.
        import pickle

        for field_name in output_tuple.get_field_names():
            # convert NaN to None to support null value conversion
            if checknull(output_tuple[field_name]):
                output_tuple[field_name] = None
            field_value = output_tuple[field_name]
            field = schema.field(field_name)
            field_type = None if field is None else field.type
            if field_type == pyarrow.binary():
                output_tuple[field_name] = b"pickle    " + pickle.dumps(field_value)

    def check_pdb(self):
        if not self.data_processor.notifiable.is_set():
            self._pause()
        else:
            self._resume()
    def _context_switch(self):
        if self.data_processor.notifiable.is_set():
            with self._dp_process_condition:
                self._dp_process_condition.notify()
                self._dp_process_condition.wait()
