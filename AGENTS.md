# AGENTS.md

## Build / run / test

- JAVA_HOME is NOT set by default. Gradle works with:
  `JAVA_HOME=/root/.sdkman/candidates/java/21.0.12+1.1-tem ./gradlew ...`
  (a Gradle-provisioned JDK also exists at `/root/.gradle/jdks/amazon_com_inc_-21-amd64-linux.2`)
- Compile: `./gradlew :app:compileJava`
- Tests (JUnit 5): `./gradlew :app:test` — test task is `finalizedBy` jacocoTestReport, so a report is always regenerated.
- Run backtest: `./gradlew run` (main class `engine.backtestMain.BBMain`). This usually FAILS without local setup below.
- Python lint config: `tools/python/ruff.toml`; dev deps in `py_requirements_dev.txt`.

## Setup gotchas (run will not work without these)

- `app/build.gradle.kts` expects a Python venv JEP jar on the run classpath:
  `.venv/lib/python3.11/site-packages/jep/jep-4.2.2.jar` (create venv, `pip install jep==4.2.2`).
- CSV data path is hardcoded: `/root/resources/SymbolsData/Portfolio` in `tools/python/src/fetch/load_csv_data.py`.
- Output path is hardcoded: `/root/resources/outputs/data/BBMR/` in `engine/constants/PublicConstants.java` (`MAIN_FOLDER`).
- `BacktestDataLoader` invokes Python via JEP using a relative script path (`../tools/python/src/fetch/load_csv_data.py`) — run from the repo root / app working dir expectations matter.
- Use absolute paths, never `~`, in hardcoded Python/JEP paths.
- `NUM_SERIES` and `DATA_LENGTH` in PublicConstants are mutable statics set at runtime by `Main`; features/constants depend on them.

## Architecture (Head -> Core -> Logic)

- Head (`engine.heads`): owns HeadState, loggers, per-candle loop. Parent abstracts: `Backtest` (backtest), `ExecutionEngine` (live). Children: `BBBacktest`, `BBExecutionEngine`.
- Core (`engine.core`): called by Head via `processCandle(...)`; MUST NOT be bypassed by calling Logic directly.
- Logic (`engine.strategyLogic`): dumb strategy; gets portfolio matrix via reValue each call.
- Core returns results through static fields on `Core.TradeContext` (`trade`, `tradeIndicesToEliminate`), not return values. trade==null means no new trade; elimination array is binary per open trade.

## Conventions

- Loggers (`engine.logger`) write files only, never print to terminal. Reporters (`engine.reporter`) print to terminal only.
- Strategy/tuning constants live in `engine.constants.PublicConstants` (static, mutable: `ENTERY_Z_SCORE`, `EXIT_Z_SCORE`, `SEQ_LENGTH`, `MR_LOOKBACK`, `TESTS_PERIOD`...). Changing them changes behavior globally.
- Features are registered via maps in `PublicConstants` (`globalFeatureMap`, `globalDerivedFeatureMap`); markers [M1]/[M2]/[M3], [D1]/[D2] in `data.feature`.
- Tests exist only under `app/src/test` for `mathUtils` and part of `cointegration`; no tests for heads/core/logic.
- `tools/fish/run_backtest.fish` clears `/root/resources/outputs/data/BackTest/BBMR` then runs `./gradlew run` and `tools/python/tools/log_monthly.py`.

## CI / meta

- No CI workflows, no pre-commit config in the repo.
- License MPL 2.0.
