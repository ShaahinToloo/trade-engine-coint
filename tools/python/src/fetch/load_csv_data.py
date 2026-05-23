from pathlib import Path

import numpy as np
import pandas as pd

base_path = Path("/home/arch/SymbolsData/Portfolio")
df_paths = sorted(
    [
        f
        for f in base_path.iterdir()
        if f.is_file() and not f.name.startswith((".", "_"))
    ]
)

time_col_name = "timestamp"
unified_index = None
ret_list = []

QUOTED_CURRENCY = "USD"
DATAFRAME_SEAPARTOR = "\t"


def _resample_ohlcv(dfs: list, timeframe: str):
    out = []

    for df in dfs:
        rdf = df.resample(timeframe, label="right", closed="right").agg(
            {
                "<OPEN>": "first",
                "<HIGH>": "max",
                "<LOW>": "min",
                "<CLOSE>": "last",
                "<TICKVOL>": "sum",
            }
        )

        rdf.dropna(inplace=True)

        out.append(rdf)

    return out


def _parse_indices(dfs: list):
    for i in range(len(dfs)):
        dfs[i]["timestamp"] = dfs[i]["<DATE>"] + " " + dfs[i]["<TIME>"]
        dfs[i].drop(
            columns=["<DATE>", "<TIME>", "<VOL>", "<SPREAD>"],
            inplace=True,
            errors="ignore",
        )
        dfs[i].set_index(time_col_name, inplace=True)
        dfs[i].index = pd.to_datetime(dfs[i].index)
    return dfs


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
        # We do bfill, bacause maybe one or some of the df(s) might have some indices at the beginning that
        # is not in the other df(s). And they remain Nan after ffill, so we do bfill.
        # Alternatively, we can just remove those indices.
        dfs[i].bfill(inplace=True)

    return dfs


def _makeQuotesSame(dfs: list):
    """
    Ensures all symbols are quoted in QUOTED_CURRENCY.

    Example:
        QUOTED_CURRENCY = "USD"

        AUDUSD   -> OK
        JP225USD -> OK
        USDCAD   -> reversed to CADUSD
    """

    if len(dfs) != len(df_paths):
        raise ValueError("dfs and df_paths length mismatch")

    out = []

    for i, df in enumerate(dfs):
        filename = df_paths[i].name

        # get symbol part before "." or "_"
        symbol = filename.split(".")[0].split("_")[0]

        if not (
            symbol.startswith(QUOTED_CURRENCY)
            or symbol.endswith(QUOTED_CURRENCY)
        ):
            raise ValueError(
                f"{filename}: invalid quote structure for "
                f"quoted currency '{QUOTED_CURRENCY}'"
            )

        # already quoted correctly
        if symbol.endswith(QUOTED_CURRENCY):
            out.append(df)
            continue

        # reverse pair
        # Example:
        # USDCAD -> CADUSD
        # invert OHLC correctly
        rdf = df.copy()

        open_ = rdf["<OPEN>"].to_numpy(dtype=np.float64)
        high_ = rdf["<HIGH>"].to_numpy(dtype=np.float64)
        low_ = rdf["<LOW>"].to_numpy(dtype=np.float64)
        close_ = rdf["<CLOSE>"].to_numpy(dtype=np.float64)

        rdf["<OPEN>"] = 1.0 / open_
        rdf["<HIGH>"] = 1.0 / low_
        rdf["<LOW>"] = 1.0 / high_
        rdf["<CLOSE>"] = 1.0 / close_

        out.append(rdf)

    return out


def prepareData():
    global ret_list, unified_index

    dfs = []
    for i, path in enumerate(df_paths):
        df = pd.read_csv(path, sep=DATAFRAME_SEAPARTOR)
        df = df.iloc[:int(len(df)*1)] # !!!
        dfs.append(df)

    dfs = _parse_indices(dfs)
    dfs = _unify_indices(dfs)
    dfs = _makeQuotesSame(dfs)
    # dfs = _resample_ohlcv(dfs, "5min")
    unified_index = dfs[0].index

    # prepare the data, must be a 3D list in the end
    # [symbol][ohlcv][row]

    ## initialize
    ret_list = []

    # convert to 3D list
    for i in range(len(dfs)):
        cols = dfs[i].columns.tolist()
        ret_list.append([])
        # We do can use cols in dfs[i][col] instead of iterating. I have found this approach more readable for myself.
        for col in cols:
            ret_list[i].append(dfs[i][col].to_numpy(dtype=np.float64).tolist())


def get_ret_list():
    return ret_list


def get_indices():
    if unified_index is None:
        raise ValueError("unified_index is None")

    idx = unified_index
    idx = pd.Index(idx).astype(str)
    return [idx.tolist()]
