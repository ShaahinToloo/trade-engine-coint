#!/usr/bin/env fish

echo "Engine Run"

# safely clear folder
find /home/arch/data/BackTest/BBMR -mindepth 1 -delete

# run pipeline
./gradlew run
python /home/arch/trade-engine-coint/tools/python/tools/log_monthly.py /home/arch/data/BackTest/BBMR/1/allTrades.csv
