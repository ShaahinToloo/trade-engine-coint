package engine.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import engine.constants.BacktestConstants;
import engine.reporter.ProgressReporter;
import engine.trade.Trade;

public class DataLogger {
	PrintWriter writer;
	StringBuilder sb;
	List<String> mainFeaturesKeySet;
	List<String> derivedFeaturesKeySet;

	Path basePath; // root folder
	Path runPath; // numbered folder for this backtest run
	boolean printHeader = true;
	String sep = ",";
	int lineCount = 0;

	public DataLogger(String folderPath) {
		try {
			basePath = LoggerUtils.pathFunc(folderPath);
			this.runPath = LoggerUtils.createNextRunFolder(basePath);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public DataLogger(String folderPath, String fileName) {
		try {
			basePath = LoggerUtils.pathFunc(folderPath);
			this.runPath = LoggerUtils.createNextRunFolder(basePath);

			Path filePath = LoggerUtils.getOriginalPath(runPath, fileName);
			this.writer = new PrintWriter(new FileWriter(filePath.toString()));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		mainFeaturesKeySet = new ArrayList<>(BacktestConstants.globalFeatureMap.keySet());
		derivedFeaturesKeySet = new ArrayList<>(BacktestConstants.globalDerivedFeatureMap.keySet());

		sb = new StringBuilder();
	}

	public DataLogger(Path runPath, String fileName) {
		try {
			this.runPath = runPath;
			this.basePath = runPath.getParent();

			Path filePath = LoggerUtils.getOriginalPath(runPath, fileName);
			this.writer = new PrintWriter(new FileWriter(filePath.toString()));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		mainFeaturesKeySet = new ArrayList<>(BacktestConstants.globalFeatureMap.keySet());
		derivedFeaturesKeySet = new ArrayList<>(BacktestConstants.globalDerivedFeatureMap.keySet());

		sb = new StringBuilder();
	}

	public void logFeatures(String currDatetimeIndex, Trade trade) {
		if (trade == null) {
			trade = new Trade();
		}
		printHeader();

		sb.append(currDatetimeIndex);
		for (String key : mainFeaturesKeySet) {
			sb.append(sep).append(BacktestConstants.globalFeatureMap.get(key)[BacktestConstants.SEQ_LENGTH - 1]);
		}
		for (String key : derivedFeaturesKeySet) {
			sb.append(sep).append(BacktestConstants.globalDerivedFeatureMap.get(key)[BacktestConstants.SEQ_LENGTH - 1]);
		}
		// sb.append(sep + peak);
		// sb.append(sep + trough);
		// sb.append(sep + trend);

		if (Double.isNaN(trade.entryPrice)) {
			sb.append(sep);
			sb.append(sep);
		} else {
			sb.append(sep).append(trade.entryPrice);
			sb.append(sep).append(trade.type);
		}
		sb.append("\n");
		lineCount += 1;
		if (lineCount % 5_000 == 0)
			flush();
	}

	private void printHeader() {
		if (printHeader) {
			printHeader = false;
			writer.print("timestamp");
			for (String key : mainFeaturesKeySet) {
				writer.print(sep + key);
			}
			for (String key : derivedFeaturesKeySet) {
				writer.print(sep + key);
			}
			writer.print(sep + "entry");
			writer.print(sep + "tradeType");
			writer.println();
		}
	}

	public void flush() {
		writer.append(sb);
		sb.setLength(0);
		lineCount = 0;
	}

	public void logBacktestResult(String result, String fileName) {
		try {
			Path txtFilePath = LoggerUtils.getOriginalPath(runPath, fileName);

			Files.writeString(txtFilePath, result, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.getStackTrace();
			System.err.printf("Failed to write backtest result to txt file: %s%n", e.getMessage());
		}
	}

	/**
	 * 
	 * @param fileName
	 * @param trades            List<List<entryIdx, entry, exitIdx, exit, pnl, type,
	 *                          id>>
	 * @param dataLength
	 * @param full_yPort
	 * @param fullDatetimeIndex
	 */
	public void logTrades(String fileName, List<Trade> trades, int dataLength, double[] full_yPort,
			double[][] full_yPort_features, List<String> full_yPort_featuresNames, List<String> fullDatetimeIndex) {
		if (full_yPort_features.length != full_yPort_featuresNames.size()) {
			// NOTE: instead of skipping we can just log the given features with custom
			// names
			System.out.println(
					"the given features for full_yPort does not match the given names for it, skipping trade logging");
			return;
		}

		List<String> headerList = new ArrayList<>(
				List.of("timestamp", "price", "tradeEntryID", "tradeType", "exitIdx", "pnlOnExit"));
		for (int i = full_yPort_features.length - 1; i >= 0; i--) {
			headerList.add(2, full_yPort_featuresNames.get(i));
		}

		StringBuilder sb = new StringBuilder();
		Path filePath = LoggerUtils.getOriginalPath(runPath, fileName);
		try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toString()))) {
			String splitter = ",";
			String header = String.join(splitter, headerList);
			writer.println(header);

			trades.sort(Comparator.comparingDouble(sublist -> sublist.entryIdx));
			int tradesSize = trades.size();
			int tradeIdx = 0;
			int lineCount = 0;

			List<List<Double>> leftTrades = new ArrayList<>(); // <exitIdx, id, pnl>
			int lastID = -1;
			long startTime = System.nanoTime();
			boolean cond = !trades.isEmpty();
			int period = dataLength/10;
			for (int i = 0; i < dataLength; i++) {
				if (i % period == 0) {
					ProgressReporter.printProgress(i, dataLength, startTime);
				}
				if (lineCount % 10_000 == 0) {
					writer.append(sb);
					sb.setLength(0);
					lineCount = 0;
				}

				String mainRow, tradeRow;

				// Raw Data
				String timestamp = fullDatetimeIndex.get(i);
				double price = full_yPort[i];

				String[] featureValues = new String[full_yPort_features.length];
				for (int f = 0; f < full_yPort_features.length; f++) {
					featureValues[f] = String.valueOf(full_yPort_features[f][i]);
				}

				// Trades
				int entryIdx = -1, exitIdx = 0, type = 0, id = 0;
				double pnl = 0;
				if (cond) {
					Trade row = trades.get(tradeIdx);
					entryIdx = row.entryIdx;
					exitIdx = row.exitIdx;
					pnl = row.pnl;
					type = row.type;
					id = row.id;
					if (id != lastID) {
						lastID = id;
						List<Double> list = List.of((double) exitIdx, (double) id, pnl);
						leftTrades.add(list);
					}
				}

				// Main Row
				String[] mainParts = new String[2 + featureValues.length];
				mainParts[0] = timestamp;
				mainParts[1] = String.valueOf(price);
				System.arraycopy(featureValues, 0, mainParts, 2, featureValues.length);

				mainRow = String.join(splitter, mainParts);

				if (i == entryIdx) {
					tradeRow = String.join(splitter,
							String.valueOf(id),
							String.valueOf(type),
							String.valueOf(exitIdx),
							String.valueOf(pnl));
					if (!(tradeIdx + 1 >= tradesSize)) {
						tradeIdx += 1;
					}
				} else {
					tradeRow = String.join(splitter,
							String.valueOf(""),
							String.valueOf(""),
							String.valueOf(""),
							String.valueOf(""));
				}

				mainRow = String.join(splitter,
						mainRow,
						tradeRow);
				sb.append(mainRow);
				sb.append("\n");
				lineCount += 1;
			}
			writer.append(sb);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("\n❌ There was an Error Saving Results to the file! 💀\n");
		}
	}

	public void logChart(Object data, String fileName) {
		Path path = LoggerUtils.getOriginalPath(runPath, fileName);

		if (!data.getClass().isArray()) {
			System.err.println(String.format("given Chart '%s' to logChart is not an Array", fileName.split("\\.")[0]));
		}

		try (PrintWriter writer = new PrintWriter(new FileWriter(path.toString()))) {
			writer.append(String.format("%s%n", fileName.split("\\.")[0]));
			for (int i = 0; i < Array.getLength(data); i++) {
				writer.append(String.format("%s%n", Array.get(data, i).toString()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(String.format("\n❌ There was an Error Saving Chart '%s' to the file! 💀\n",
					fileName.split("\\.")[0]));
		}
	}

	public Path getRunPath() {
		return this.runPath;
	}
}
