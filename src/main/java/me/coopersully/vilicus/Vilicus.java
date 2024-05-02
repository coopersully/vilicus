package me.coopersully.vilicus;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
            System.err.println("Failed to create directories.");
            e.printStackTrace();
        }

        VilicusConfig config = new VilicusConfig();
        // Determine the server file name from command line arguments or use default
        String serverFileName = args.length > 0 ? args[0] : ServerUpdater.DEFAULT_FILE_NAME;

        // Check and perform updates if necessary
        if (config.shouldUpdateApi()) {
            System.out.println("Attempting to update server API...");
            try {
                ServerUpdater.updateAPI(args);
            } catch (Exception e) {
                System.err.println("Failed to update server API.");
                e.printStackTrace();
            }
        }

        if (config.shouldUpdatePluginNames()) {
            System.out.println("Attempting to organize plugin structure...");
            PluginRenamer.renamePlugins();
        }

        // Start the Minecraft server with specified configurations
        runServer(serverFileName, config);
    }

    private static void runServer(String serverFileName, @NotNull VilicusConfig config) {
        ProcessBuilder processBuilder = new ProcessBuilder(config.getAllFlags(serverFileName));
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        try {
            System.out.println("Starting server...");
            Process process = processBuilder.start();
            // Forward console input to the server process
            forwardInputToProcess(process);
            // Wait for the server process to complete
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to start the server.");
            e.printStackTrace();
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
                System.err.println("Failed to forward input.");
                e.printStackTrace();
            }
        });

        inputThread.setDaemon(true); // Allows the JVM to exit even if this thread is running
        inputThread.start();
    }
}
