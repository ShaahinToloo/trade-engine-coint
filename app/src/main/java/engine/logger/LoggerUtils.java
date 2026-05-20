package engine.logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LoggerUtils {

	/**
	 * This used to number the files, now we just number folders
	 * 
	 * @param fileName
	 * @return
	 */
	@Deprecated
	private static Path __getOriginalPath(Path basePath, String fileName) {
		Path filePath = Path.of(basePath.toString(), fileName);

		while (true) {
			if (Files.exists(filePath)) {
				String name = fileName.split("\\.")[0];
				String postfix = fileName.split("\\.")[1];

				String lastNumber = name.replaceAll(".*[^0-9]", "");
				if (lastNumber.isEmpty()) {
					name += "1";
				} else {
					int lN = Integer.parseInt(lastNumber);
					name = name.replace(String.valueOf(lN), "");
					name += String.valueOf(lN + 1);
				}
				fileName = name + "." + postfix;
				filePath = Path.of(basePath.toString(), fileName);
			} else {
				break;
			}
		}
		return filePath;
	}

	public static Path getOriginalPath(Path runPath, String fileName) {
		return Path.of(runPath.toString(), fileName);
	}

	public static Path createNextRunFolder(Path basePath) throws IOException {
		int runId = 1;
		Path candidate;

		while (true) {
			candidate = Path.of(basePath.toString(), String.valueOf(runId));
			if (!Files.exists(candidate)) {
				Files.createDirectories(candidate);
				return candidate;
			}
			runId++;
		}
	}

	public static Path pathFunc(String folderPath) throws IOException {
		Path basePath = Path.of(folderPath);
		if (!Files.exists(basePath)) {
			Files.createDirectories(basePath);
		}
        return basePath;
	}
}
