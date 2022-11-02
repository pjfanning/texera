from queue import Queue
from threading import Event
from typing import Optional

from core.models import Tuple, Operator
from core.util.runnable.runnable import Runnable


class DataProcessor(Runnable):
    def __init__(
        self,
        input_queue: Queue,
        output_queue: Queue,
        operator: Optional[Operator],
        dp_condition,
    ):
        self._input_queue = input_queue
        self._output_queue = output_queue
        self._operator = operator
        self._dp_condition = dp_condition
        self._finished_current = Event()
        self._running = Event()

    def run(self) -> None:
        with self._dp_condition:
            self._dp_condition.wait()
        self._running.set()
        while self._running.is_set():
            tuple_, input_ = self._input_queue.get()
            for output in (
                self._operator.get().process_tuple(tuple_, input_)
                if isinstance(tuple_, Tuple)
                else self._operator.get().on_finish(input_)
            ):
                self._output_queue.put(None if output is None else Tuple(output))
                with self._dp_condition:
                    self._dp_condition.notify()
                    self._dp_condition.wait()

            self._finished_current.set()
            with self._dp_condition:
                self._dp_condition.notify()
                self._dp_condition.wait()
