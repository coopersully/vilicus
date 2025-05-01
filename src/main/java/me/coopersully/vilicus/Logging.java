package me.coopersully.vilicus;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {
    private static final Logger logger = Logger.getLogger("Vilicus");

    static {
        // Configure logger to use simple format
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$s] %5$s%6$s%n");
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warning(String message) {
        logger.warning(message);
    }

    public static void severe(String message) {
        logger.severe(message);
    }

    public static void severe(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    public static void debug(String message) {
        logger.fine(message);
    }

    public static void trace(String message) {
        logger.finer(message);
    }
} 