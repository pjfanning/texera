from abc import abstractmethod
from typing import TypeVar, Sized, Optional, Generic
from typing_extensions import Protocol

T_con = TypeVar("T_con", contravariant=True)
K_con = TypeVar("K_con", contravariant=True)
T_co = TypeVar("T_co", covariant=True)


class Putable(Protocol[T_con]):
    @abstractmethod
    def put(self, item: T_con) -> None:
        pass


class KeyedPutable(Protocol[K_con, T_con]):
    @abstractmethod
    def put(self, key: K_con, item: T_con) -> None:
        pass


class Getable(Protocol[T_co]):
    @abstractmethod
    def get(self) -> T_co:
        pass


class FlushedGetable(Protocol[T_co]):
    @abstractmethod
    def get(self, flush: bool) -> T_co:
        pass


class EmtpyCheckable(Sized):
    @abstractmethod
    def is_empty(self) -> bool:
        pass


class KeyedEmtpyCheckable(Sized):
    @abstractmethod
    def is_empty(self, key: Optional[K_con] = None) -> bool:
        pass
