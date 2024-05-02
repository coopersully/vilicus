package me.coopersully.vilicus;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServerUpdater {

    private static final String API_BASE_URL = "https://api.purpurmc.org/v2/purpur/";
    public static final String DEFAULT_FILE_NAME = "server.jar";
    private static final String HISTORY_FILE_NAME = "vilicus/.server_api_version.yml";

    public static void updateAPI(String[] args) throws Exception {
        String currentVersion = getCurrentVersion();
        System.out.println("Current server version: " + (currentVersion != null ? currentVersion : "UNKNOWN"));

        String latestVersion = getLatestVersion();
        if (latestVersion == null) {
            System.out.println("Failed to fetch the latest server version.");
            return;
        }

        System.out.println("Latest server version: " + latestVersion);
        if (latestVersion.equals(currentVersion)) {
            System.out.println("Server is up-to-date with version " + latestVersion);
            return;
        }

        // Download and install the latest version
        System.out.println("Updating server to version " + latestVersion + "...");
        File file = getFilePath(args.length > 0 ? args[0] : DEFAULT_FILE_NAME);
        saveURLtoFile(new URL(API_BASE_URL + latestVersion + "/latest/download"), file);

        updateVersionHistory(latestVersion);
    }

    // Fetch the latest version from the API
    private static String getLatestVersion() throws IOException {
        URL url = new URL(API_BASE_URL);
        try (InputStream input = url.openStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);
            if (data != null && data.containsKey("versions")) {
                List<String> versions = (List<String>) data.get("versions");
                if (!versions.isEmpty()) {
                    return versions.get(versions.size() - 1); // Return the last item, the latest version
                }
            }
        }
        return null;
    }

    // Save the downloaded file to the specified path
    private static void saveURLtoFile(URL url, File file) throws IOException {
        Files.copy(url.openStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    // Update the version history in YML format
    private static void updateVersionHistory(String version) throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", version);

        Yaml yaml = new Yaml();
        try (FileWriter writer = new FileWriter(getFilePath(HISTORY_FILE_NAME))) {
            yaml.dump(data, writer);
        }
    }

    // Retrieve the current version from the history file
    private static String getCurrentVersion() {
        File file = getFilePath(HISTORY_FILE_NAME);
        if (file.exists()) {
            Yaml yaml = new Yaml();
            try (FileInputStream fis = new FileInputStream(file)) {
                Map<String, Object> data = yaml.load(fis);
                if (data != null) {
                    return (String) data.get("version");
                }
            } catch (IOException e) {
                // If reading the history file fails, assume corrupt or outdated and regenerate it
                System.err.println("Error reading history file, regenerating: " + e.getMessage());
                updateServerToLatest();
            }
        }
        return null;
    }

    // Helper method to force server update to the latest version
    private static void updateServerToLatest() {
        try {
            String latestVersion = getLatestVersion();
            if (latestVersion != null) {
                File file = getFilePath(DEFAULT_FILE_NAME);
                saveURLtoFile(new URL(API_BASE_URL + latestVersion + "/latest/download"), file);
                updateVersionHistory(latestVersion);
            }
        } catch (Exception e) {
            System.err.println("Failed to force update server: " + e.getMessage());
        }
    }

    private static File getFilePath(String fileName) {
        return new File(System.getProperty("user.dir"), fileName);
    }
}
