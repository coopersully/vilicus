package me.coopersully.vilicus;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Vilicus {
    public static void main(String[] args) throws Exception {

        System.out.println("""
                               
                               __        __
                \\  / | |    | /  ` |  | /__`
                 \\/  | |___ | \\__, \\__/ .__/
                 
                """);

        File vilicusDir = new File("vilicus");
        vilicusDir.mkdir();

        VilicusConfig config = new VilicusConfig();

        String serverFileName = args.length > 0 ? args[0] : ServerUpdater.DEFAULT_FILE_NAME;

        if (config.shouldUpdateApi()) {
            System.out.println("Attempting to update server API...");
            ServerUpdater.updateAPI(args);
            System.out.println();
        }

        if (config.shouldUpdatePluginNames()) {
            System.out.println("Attempting to organize plugin structure...");
            PluginRenamer.renamePlugins();
            System.out.println();
        }

        runServer(serverFileName, config);
    }

    private static void runServer(String serverFileName, @NotNull VilicusConfig config) {

        ProcessBuilder processBuilder = new ProcessBuilder(
                config.getAllFlags(serverFileName)
        );

        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        try {
            System.out.println("Starting server...");
            Process process = processBuilder.start();

            // Forward input from the console to the server process
            forwardInputToProcess(process);

            process.waitFor();
        } catch (Exception e) {
            System.out.println("Failed to start the server.");
            e.printStackTrace();
        }
    }

    private static void forwardInputToProcess(Process process) {
        Thread inputThread = new Thread(() -> {
            try {
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                OutputStream processInput = process.getOutputStream();

                String inputLine;
                while ((inputLine = consoleReader.readLine()) != null) {
                    processInput.write((inputLine + System.lineSeparator()).getBytes());
                    processInput.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        inputThread.setDaemon(true);
        inputThread.start();
    }
}
