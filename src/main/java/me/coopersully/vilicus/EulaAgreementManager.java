package me.coopersully.vilicus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class EulaAgreementManager {

    private static final String EULA_FILE_NAME = "eula.txt";

    public static void ensureEulaAgreement() {
        File eulaFile = new File(EULA_FILE_NAME);
        try {
            // Check if the EULA file exists and contains the expected content
            if (eulaFile.exists() && checkEulaContent(eulaFile)) {
                System.out.println("EULA agreement is up to date.");
            } else {
                // Create or update the EULA file by copying it from resources
                System.out.println("Updating EULA agreement file...");
                copyEulaFromResource(eulaFile);
                System.out.println("EULA agreement has been updated.");
            }
        } catch (IOException e) {
            System.err.println("Failed to create or update the EULA agreement file: " + e.getMessage());
        }
    }

    private static boolean checkEulaContent(File eulaFile) throws IOException {
        // Check if the file content matches the resource content
        try (InputStream resourceStream = EulaAgreementManager.class.getResourceAsStream("/" + EULA_FILE_NAME)) {
            String resourceContent = new String(resourceStream.readAllBytes());
            String fileContent = Files.readString(eulaFile.toPath());
            return fileContent.equals(resourceContent);
        }
    }

    private static void copyEulaFromResource(File eulaFile) throws IOException {
        // Copy the EULA text from resources to the eula.txt file
        try (InputStream inputStream = EulaAgreementManager.class.getResourceAsStream("/" + EULA_FILE_NAME);
             FileOutputStream outputStream = new FileOutputStream(eulaFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }
}
