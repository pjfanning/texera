from queue import Queue
from threading import Event

from loguru import logger
from overrides import overrides
from pampy import match

from core.architecture.managers import Context
from core.models import Tuple, InputExhausted
from core.models.tdb import QueueIn, QueueOut, Tdb
from core.util import DoubleBlockingQueue, StoppableQueueBlockingRunnable, IQueue
from core.util.runnable.runnable import Runnable


class DataProcessor(StoppableQueueBlockingRunnable):
    def __init__(
        self, input_queue: DoubleBlockingQueue, output_queue: Queue, dp_condition, context: Context
    ):
        super().__init__(self.__class__.__name__, input_queue)
        self._input_queue = input_queue
        self._output_queue = output_queue
        self._operator = context.dp._operator
        self._dp_condition = dp_condition
        self._finished_current = Event()
        self._running = Event()
        self._context = context

        self.notifiable = Event()
        self.notifiable.set()
        queue_in, queue_out = QueueIn(), QueueOut(context.dp._async_rpc_client)
        self.debug_input_queue = queue_in.queue

        def switch_channel():
            self.notifiable.clear()
            with self._dp_condition:
                self._dp_condition.notify()

        self._tdb = Tdb(queue_in, queue_out, switch_channel)


    @overrides
    def receive(self, next_entry: IQueue.QueueElement) -> None:
        """
        Main entry point of the DataProcessor. Upon receipt of an next_entry, process it respectfully.
        :param next_entry: An entry from input_queue, could be one of the followings:
                    1. a ControlElement;
                    2. a DataElement.
        """
        print("receiving ", next_entry)
        match(
            next_entry,
            (Tuple, int), self.process_tuple,
            (InputExhausted, int), self.process_tuple,
            str, self._process_breakpoint
        )
        self.context_switch()
        self.check_and_process_breakpoint()
    def pre_start(self) -> None:
        with self._dp_condition:
            self._dp_condition.wait()
    def _process_breakpoint(self, _):
        self._tdb.set_trace()
    def process_tuple(self, tuple_: Tuple, port: int):
        while not self._finished_current.is_set():
            try:
                self.context_switch()
                self.check_and_process_breakpoint()
                operator = self._operator.get()

                output_iterator = (
                    operator.process_tuple(tuple_, port)
                    if isinstance(tuple_, Tuple)
                    else operator.on_finish(port)
                )
                for output in output_iterator:
                    self._output_queue.put(None if output is None else Tuple(output))
                    self.context_switch()
                    self.check_and_process_breakpoint()

                # current tuple finished successfully
                print("done processing current", tuple_)
                self._finished_current.set()


            except Exception as err:
                logger.exception(err)
                self._context.dp.report_exception()
                self._context.dp._pause()
                self.context_switch()

    def check_and_process_breakpoint(self):
        while not self._input_queue.main_empty():
            self._process_breakpoint(self.interruptible_get())
    def context_switch(self):
        with self._dp_condition:
            self._dp_condition.notify()
            self._dp_condition.wait()

    def stop(self):
        self._running.clear()
