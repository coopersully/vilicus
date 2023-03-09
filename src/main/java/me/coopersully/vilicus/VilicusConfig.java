package me.coopersully.vilicus;

import java.io.*;
import java.util.Map;

import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.Constructor;

public class VilicusConfig {
    private boolean updateApi;
    private boolean updatePluginNames;

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
            this.updateApi = (Boolean) yamlData.get("update_api");
            this.updatePluginNames = (Boolean) yamlData.get("update_plugin_names");
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
}
