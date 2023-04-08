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

        if (!configFile.exists()) {
            // If the config file doesn't exist, create it with default values
            try {
                configFile.createNewFile();
                copyConfigFile(configFile);
            } catch (IOException e) {
                System.out.println("Failed to create config file");
                e.printStackTrace();
            }
        }

        // Load the config file into memory
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(configFile);
            Map<String, Object> yamlData = yaml.load(inputStream);
            System.out.println(yamlData);
            this.updateApi = (Boolean) yamlData.get("update_api");
            this.updatePluginNames = (Boolean) yamlData.get("update_plugin_names");

            // Load on_launch settings
            Map<String, Object> onLaunchData = (Map<String, Object>) yamlData.get("on_launch");

            // Load heap settings
            Map<String, Object> heapData = (Map<String, Object>) onLaunchData.get("heap");
            this.initialHeapSize = (Integer) heapData.get("initial");
            this.maximumHeapSize = (Integer) heapData.get("maximum");

            // Load additional flags
            List<String> flagsList = (List<String>) onLaunchData.get("flags");
            this.additionalFlags = flagsList.toArray(new String[0]);

            inputStream.close();
        } catch (IOException e) {
            System.out.println("Failed to load config file");
            e.printStackTrace();
        }
    }


    private void copyConfigFile(File targetFile) throws IOException {
        // Get the reference file's InputStream from the jar
        InputStream inputStream = getClass().getResourceAsStream("/config.yml");

        // Write the reference file's InputStream to the target file's OutputStream
        OutputStream outputStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        // Close the streams
        inputStream.close();
        outputStream.close();
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
        return flagsList;
    }


}
