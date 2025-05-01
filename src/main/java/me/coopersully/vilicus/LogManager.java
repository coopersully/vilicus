package me.coopersully.vilicus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class LogManager {
    private static final String LOGS_DIRECTORY = "logs";
    private static final List<String> LOG_EXTENSIONS = Arrays.asList(".log", ".gz", ".txt");
    private static final long MIN_FILE_AGE_DAYS = 1; // Minimum age before deletion

    public static void manageLogs(VilicusConfig config) {
        if (!config.isAutoDeleteEnabled()) {
            Logging.info("Auto-delete is disabled.");
            return;
        }

        int retentionDays = config.getRetentionDays();
        if (retentionDays < MIN_FILE_AGE_DAYS) {
            Logging.warning("Retention period is less than minimum allowed (" + MIN_FILE_AGE_DAYS + " days). Using minimum value.");
            retentionDays = (int) MIN_FILE_AGE_DAYS;
        }

        File logsDir = new File(LOGS_DIRECTORY);
        if (!logsDir.exists()) {
            Logging.info("No logs directory found.");
            return;
        }

        if (!logsDir.canRead() || !logsDir.canWrite()) {
            Logging.severe("Logs directory is not readable/writable. Check permissions.");
            return;
        }

        File[] files = logsDir.listFiles();
        if (files == null) {
            Logging.severe("Error reading logs directory.");
            return;
        }

        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
        int deletedCount = 0;
        int failedCount = 0;
        long totalSize = 0;

        for (File file : files) {
            if (!file.isFile() || !isLogFile(file)) {
                continue;
            }

            try {
                BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                LocalDate fileDate = attrs.lastModifiedTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                if (fileDate.isBefore(cutoffDate)) {
                    long fileSize = file.length();
                    if (file.delete()) {
                        deletedCount++;
                        totalSize += fileSize;
                        Logging.info("Deleted log file: " + file.getName() + " (" + formatFileSize(fileSize) + ")");
                    } else {
                        failedCount++;
                        Logging.warning("Failed to delete log file: " + file.getName());
                    }
                }
            } catch (IOException e) {
                failedCount++;
                Logging.severe("Error processing file: " + file.getName(), e);
            }
        }

        // Log summary
        if (deletedCount > 0) {
            Logging.info(String.format("Log cleanup completed. Deleted %d files (%s), %d failed.",
                    deletedCount, formatFileSize(totalSize), failedCount));
        } else {
            Logging.info("No log files required deletion.");
        }
    }

    private static boolean isLogFile(File file) {
        String name = file.getName().toLowerCase();
        return LOG_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
    }
}
