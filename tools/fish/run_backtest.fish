#!/usr/bin/env fish

echo "Engine Run"

# safely clear folder
find /root/resources/outputs/data/BackTest/BBMR -mindepth 1 -delete

# run pipeline
./gradlew run
python /root/codes/trade-engine-coint/tools/python/tools/log_monthly.py /root/resources/outputs/data/BackTest/BBMR/1/allTrades.csv
