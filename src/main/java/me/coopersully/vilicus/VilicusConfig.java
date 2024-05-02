package me.coopersully.vilicus;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VilicusConfig {
    private boolean updateApi;
    private boolean updatePluginNames;
    private int initialHeapSize;
    private int maximumHeapSize;
    private String[] additionalFlags;

    public VilicusConfig() {
        File configFile = new File("vilicus/config.yml");
        createConfigFileIfNotExists(configFile);
        loadConfig(configFile);
    }

    private void createConfigFileIfNotExists(File configFile) {
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                copyConfigFile(configFile);
            } catch (IOException e) {
                System.out.println("Failed to create config file.");
                e.printStackTrace();
            }
        }
    }

    private void loadConfig(File configFile) {
        try (InputStream inputStream = new FileInputStream(configFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(inputStream);
            updateApi = (Boolean) yamlData.get("update_api");
            updatePluginNames = (Boolean) yamlData.get("update_plugin_names");

            Map<String, Object> onLaunchData = (Map<String, Object>) yamlData.get("on_launch");
            Map<String, Object> heapData = (Map<String, Object>) onLaunchData.get("heap");
            initialHeapSize = (Integer) heapData.get("initial");
            maximumHeapSize = (Integer) heapData.get("maximum");

            List<String> flagsList = (List<String>) onLaunchData.get("flags");
            additionalFlags = flagsList.toArray(new String[0]);
        } catch (IOException e) {
            System.out.println("Failed to load config file.");
            e.printStackTrace();
        }
    }

    private void copyConfigFile(File targetFile) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/config.yml");
             OutputStream outputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    public boolean shouldUpdateApi() {
        return updateApi;
    }

    public boolean shouldUpdatePluginNames() {
        return updatePluginNames;
    }

    public String getInitialHeapSize() {
        return initialHeapSize + "M";
    }

    public String getMaximumHeapSize() {
        return maximumHeapSize + "M";
    }

    public String[] getAdditionalFlags() {
        return additionalFlags;
    }

    public List<String> getAllFlags(String fileName) {
        List<String> flagsList = new ArrayList<>();
        flagsList.add("java");
        flagsList.add("-Xms" + getInitialHeapSize());
        flagsList.add("-Xmx" + getMaximumHeapSize());
        Collections.addAll(flagsList, getAdditionalFlags());
        flagsList.add("-jar");
        flagsList.add(fileName);
        flagsList.add("nogui");
        return flagsList;
    }
}
