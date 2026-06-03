import os
import queue
import threading
import numpy as np
import pandas as pd


ret_list = []
unified_index = None

_csv_queue = queue.Queue(-1)
_csv_worker_thread = None


def _csv_backend_worker():
    """Runs continuously in a separate background thread.

    Pulls DataFrames from the queue and appends them to their respective CSVs.
    """
    while True:
        item = _csv_queue.get()
        if item is None:
            break

        df, filename = item
        try:
            file_exists = os.path.exists(filename)

            df.to_csv(
                filename,
                mode="a",
                header=not file_exists,
                index=True,
            )

        except Exception as e:
            print(f"Async CSV Write Error for {filename}: {e}", flush=True)
        finally:
            _csv_queue.task_done()


def init_async_csv_logging():
    """Call this ONCE from Java at startup to spin up the CSV background thread."""
    global _csv_worker_thread
    if _csv_worker_thread is not None:
        return  # Thread is already running

    _csv_worker_thread = threading.Thread(target=_csv_backend_worker, daemon=True)
    _csv_worker_thread.start()


def _unify_indices(dfs: list):
    global unified_index
    unified_index = pd.DatetimeIndex([])
    for df in dfs:
        unified_index = unified_index.union(df.index)

    unified_index = unified_index.sort_values()
    unified_index = unified_index.tz_localize("EET", ambiguous="infer")
    unified_index = unified_index.tz_convert("UTC")
    unified_index = unified_index.tz_localize(None)

    for i, df in enumerate(dfs):
        if df.index.has_duplicates:
            raise ValueError(
                f'{i}, "HAS DUPLICATES:", {df.index[df.index.duplicated()]}'
            )

    dfs = [df.reindex(unified_index) for df in dfs]

    for i in range(len(dfs)):
        dfs[i].ffill(inplace=True)
        dfs[i].bfill(inplace=True)

    return dfs


def _makeQuotesSame(dfs: list, symbols_to_reverse: list):
    out = dfs

    for i in symbols_to_reverse:
        rdf = dfs[i]

        open_ = rdf["Open"].to_numpy(dtype=np.float64)
        high_ = rdf["High"].to_numpy(dtype=np.float64)
        low_ = rdf["Low"].to_numpy(dtype=np.float64)
        close_ = rdf["Close"].to_numpy(dtype=np.float64)

        rdf["Open"] = 1.0 / open_
        rdf["High"] = 1.0 / low_
        rdf["Low"] = 1.0 / high_
        rdf["Close"] = 1.0 / close_

    return out


def cleanDataLiveInit(dfs, symbols_to_reverse, symbol_names=None):
    """Cleans data and automatically dumps individual dataframes to CSVs in the background.

    :param symbol_names: Optional list of strings matching the names of the
    symbols in `dfs`.
    """
    global ret_list, unified_index

    dfs = _unify_indices(dfs)
    dfs = _makeQuotesSame(dfs, symbols_to_reverse)
    unified_index = dfs[0].index

    ret_list = []
    for i in range(len(dfs)):
        cols = dfs[i].columns.tolist()
        ret_list.append([])
        for col in cols:
            ret_list[i].append(dfs[i][col].to_numpy(dtype=np.float64).tolist())

    for i, df in enumerate(dfs):
        symbol_id = symbol_names[i] if symbol_names else f"symbol_{i}"
        csv_filename = f"{symbol_id}.csv"
        _csv_queue.put((df.copy(), csv_filename))


def get_ret_list():
    return ret_list


def get_indices():
    if unified_index is None:
        raise ValueError("unified_index is None")
    idx = unified_index
    idx = pd.Index(idx).astype(str)
    return [idx.tolist()]
