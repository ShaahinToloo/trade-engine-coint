package engine.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InfoLogger {

    private PrintWriter writer;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public InfoLogger(String folderPath, String fileName) {
        try {
            Path basePath = LoggerUtils.pathFunc(folderPath);
            Path runPath = LoggerUtils.createNextRunFolder(basePath);
            Path filePath = LoggerUtils.getOriginalPath(runPath, fileName);

            writer = new PrintWriter(new FileWriter(filePath.toString(), true), true); // append mode, auto-flush
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String level, String message) {
        if (writer != null) {
            String timeStamp = LocalDateTime.now().format(formatter);
            writer.println("[" + timeStamp + "] [" + level + "] " + message);
        }
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void warn(String message) {
        log("WARNING", message);
    }

    public void err(String message) {
        log("ERROR", message);
    }

    public void debug(String message) {
        log("DEBUG", message);
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}