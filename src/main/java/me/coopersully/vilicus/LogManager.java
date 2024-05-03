package me.coopersully.vilicus;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class LogManager {

    private static final String LOGS_DIRECTORY = "logs";

    public static void manageLogs(VilicusConfig config) {
        if (!config.isAutoDeleteEnabled()) {
            System.out.println("Auto-delete is disabled.");
            return;
        }

        int retentionDays = config.getRetentionDays();
        File logsDir = new File(LOGS_DIRECTORY);
        if (!logsDir.exists()) {
            System.out.println("No logs directory found.");
            return;
        }

        File[] files = logsDir.listFiles();
        if (files == null) {
            System.out.println("Error reading logs directory.");
            return;
        }

        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".log")) {
                LocalDate fileDate = new Date(file.lastModified()).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                if (fileDate.isBefore(cutoffDate)) {
                    if (file.delete()) {
                        System.out.println("Deleted log file: " + file.getName());
                    } else {
                        System.out.println("Failed to delete log file: " + file.getName());
                    }
                }
            }
        }
    }
}
