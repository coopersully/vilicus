package me.coopersully.vilicus;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static me.coopersully.vilicus.EulaAgreementManager.ensureEulaAgreement;

public class Vilicus {
    public static void main(String[] args) {
        // Print ASCII art header for the application
        System.out.println("""
                 .----------------.\s
                | .--------------. |
                | | ____   ____  | |
                | ||_  _| |_  _| | |
                | |  \\ \\   / /   | |
                | |   \\ \\ / /    | |
                | |    \\ ' /     | |
                | |     \\_/      | |
                | |              | |
                | '--------------' |
                 '----------------'\s
                """);

        // Create the config directory if it doesn't exist
        Path configDirectory = Path.of("vilicus");
        try {
            Files.createDirectories(configDirectory);
        } catch (IOException e) {
            Logging.severe("Failed to create directories.", e);
        }

        VilicusConfig config = new VilicusConfig();
        // Determine the server file name from command line arguments or use default
        String serverFileName = args.length > 0 ? args[0] : ServerUpdater.DEFAULT_FILE_NAME;

        // Check and perform updates if necessary
        if (config.shouldUpdateApi()) {
            Logging.info("Attempting to update server API...");
            try {
                ServerUpdater.updateAPI(args);
            } catch (Exception e) {
                Logging.severe("Failed to update server API.", e);
            }
        }

        if (config.shouldUpdatePluginNames()) {
            Logging.info("Attempting to organize plugin structure...");
            PluginRenamer.renamePlugins();
        }

        if (config.shouldForceUnlockSessions()) {
            Logging.info("Attempting to unlock world sessions...");
            SessionUnlocker.unlockSessions();
        }

        LogManager.manageLogs(config);

        // Start the Minecraft server with specified configurations
        ensureEulaAgreement();
        runServer(serverFileName, config);
    }

    private static void runServer(String serverFileName, @NotNull VilicusConfig config) {
        ProcessBuilder processBuilder = new ProcessBuilder(config.getAllFlags(serverFileName));
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        try {
            Logging.info("Starting server...");
            Process process = processBuilder.start();
            // Forward console input to the server process
            forwardInputToProcess(process);
            // Wait for the server process to complete
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            Logging.severe("Failed to start the server.", e);
        }
    }

    private static void forwardInputToProcess(Process process) {
        Thread inputThread = new Thread(() -> {
            // Create readers and streams for handling console input
            try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                 OutputStream processInput = process.getOutputStream()) {
                String inputLine;
                while ((inputLine = consoleReader.readLine()) != null) {
                    processInput.write((inputLine + System.lineSeparator()).getBytes());
                    processInput.flush();
                }
            } catch (IOException e) {
                Logging.severe("Failed to forward input.", e);
            }
        });

        inputThread.setDaemon(true); // Allows the JVM to exit even if this thread is running
        inputThread.start();
    }
}
