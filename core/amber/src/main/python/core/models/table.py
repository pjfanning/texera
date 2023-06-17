from typing import TypeVar, Protocol, Sized, Container, Iterable, Iterator, _T_co

import pandas

from core.models import TupleLike


# TableLike = TypeVar("TableLike", pandas.DataFrame, pandas.DataFrame)

class TableLike(
    Protocol,
    Sized,
    Container,
    Iterable[TupleLike]
):
    def __getitem__(self, item):
        ...

    def __setitem__(self, key, value):
        ...


class Table(TableLike, pandas.DataFrame):

    def __init__(self, table_like: TableLike):
        super().__init__(table_like)
