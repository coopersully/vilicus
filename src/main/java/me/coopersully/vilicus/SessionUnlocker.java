package me.coopersully.vilicus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SessionUnlocker {
    private static final String SESSION_LOCK_FILE = "session.lock";
    private static final String LEVEL_DAT_FILE = "level.dat";
    private static final String[] WORLD_DATA_FILES = {
        LEVEL_DAT_FILE,
        "region",
        "data",
        "entities",
        "poi"
    };

    public static void unlockSessions() {
        File serverDirectory = new File(System.getProperty("user.dir"));
        List<File> worldDirectories = findWorldDirectories(serverDirectory);

        if (worldDirectories.isEmpty()) {
            Logging.info("No world directories found.");
            return;
        }

        for (File worldDir : worldDirectories) {
            Path sessionLockPath = Paths.get(worldDir.getAbsolutePath(), SESSION_LOCK_FILE);
            if (Files.exists(sessionLockPath)) {
                try {
                    Files.delete(sessionLockPath);
                    Logging.info("Deleted session lock in " + worldDir.getName());
                } catch (IOException e) {
                    Logging.severe("Failed to delete session lock in " + worldDir.getName(), e);
                }
            }
        }
    }

    private static List<File> findWorldDirectories(File serverDirectory) {
        List<File> worldDirectories = new ArrayList<>();
        File[] directories = serverDirectory.listFiles(File::isDirectory);

        if (directories == null) {
            return worldDirectories;
        }

        for (File dir : directories) {
            // Check if directory contains world data
            if (isWorldDirectory(dir)) {
                worldDirectories.add(dir);
            }
        }

        return worldDirectories;
    }

    private static boolean isWorldDirectory(File directory) {
        // Check for level.dat first as it's the most reliable indicator
        if (new File(directory, LEVEL_DAT_FILE).exists()) {
            return true;
        }

        // If no level.dat, check for other world data files
        int matchingFiles = 0;
        for (String worldFile : WORLD_DATA_FILES) {
            File file = new File(directory, worldFile);
            if (file.exists()) {
                matchingFiles++;
            }
        }

        // Consider it a world if it has at least 2 world data files
        return matchingFiles >= 2;
    }
} 