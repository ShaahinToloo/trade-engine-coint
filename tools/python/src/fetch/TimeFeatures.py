import datetime
import pandas as pd
import numpy as np


class TimeFeatures:
    def __init__(self, gmt_offset):
        self.gmt_offset = gmt_offset

    def _extract_hour(df: pd.DataFrame) -> pd.DataFrame:
        df["hour"] = df.index.hour
        return df

    def _extract_day_of_week(df: pd.DataFrame) -> pd.DataFrame:
        df["day_of_week"] = df.index.dayofweek  # Monday=0, Sunday=6
        return df

    def _extract_session(df):
        """
        Rough Forex sessions based on UTC time:
        - Asia:     00:00 - 08:00
        - London:   08:00 - 16:00
        - New York: 16:00 - 00:00
        """
        def get_session(hour):
            if 0 <= hour < 8:
                return "0"  # Asia
            elif 8 <= hour < 16:
                return "1"  # London
            else:
                return "2"  # New York

        df["session"] = df.index.hour.map(get_session)
        return df

    def _extract_is_weekend(df: pd.DataFrame) -> pd.DataFrame:
        df["is_weekend"] = (df.index.dayofweek >= 5).astype(int)  # Saturday/Sunday
        return df

    def _extract_is_market_open(df: pd.DataFrame) -> pd.DataFrame:
        """
        Market generally open from Sunday 22:00 UTC to Friday 22:00 UTC.
        """
        def is_open(ts):
            weekday = ts.weekday()
            hour = ts.hour
            minute = ts.minute
            if weekday == 6 and hour >= 22:
                return True
            elif weekday in range(0, 5):
                return True
            elif weekday == 5 and hour < 22:
                return True
            return False

        df["is_market_open"] = df.index.map(is_open).astype(int)
        return df

    def shift_index_hours(df: pd.DataFrame, hours: int):
        df2 = df.copy(deep=True)
        df2.index = pd.to_datetime(df2.index) + pd.Timedelta(hours=hours)
        return df2

    def apply_is_day_start(df: pd.DataFrame):
        df = shift_index_hours(df, -self.gmt_offset)

        df = df.sort_index()
        curr_day_idx = df.index.normalize().astype(str).tolist()

        df['isDayStart'] = 0
        for j in range(1, len(curr_day_idx)):
            if curr_day_idx[j] != curr_day_idx[j-1]:
                df.iloc[j, df.columns.get_loc('isDayStart')] = 1

        df = shift_index_hours(df, self.gmt_offset)

        return df

    def add_time_features(df: pd.DataFrame) -> pd.DataFrame:
        if not pd.api.types.is_datetime64_any_dtype(df.index):
            raise TypeError("Index must be of datetime64 type")

        df = shift_index_hours(df, -self.gmt_offset)

        df = extract_hour(df)
        df = extract_day_of_week(df)
        df = extract_session(df)
        df = extract_is_weekend(df)
        df = extract_is_market_open(df)
        
        df = shift_index_hours(df, self.gmt_offset)
        return df
