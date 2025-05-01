package me.coopersully.vilicus;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VilicusConfig {
    private boolean updateApi;
    private String preferredVersion;
    private boolean updatePluginNames;
    private int initialHeapSize;
    private int maximumHeapSize;
    private String[] additionalFlags;
    private boolean enableAutoDelete;
    private int retentionDays;
    private boolean forceUnlockSessions;

    public VilicusConfig() {
        File configFile = new File("vilicus/config.yml");
        createConfigFileIfNotExists(configFile);
        loadConfig(configFile);
    }

    private void createConfigFileIfNotExists(File configFile) {
        if (!configFile.exists()) {
            try {
                File parent = configFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                copyConfigFile(configFile);
            } catch (IOException e) {
                Logging.severe("Failed to create config file", e);
            }
        }
    }

    private void loadConfig(File configFile) {
        try (InputStream inputStream = new FileInputStream(configFile);
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            Yaml yaml = new Yaml();
            @SuppressWarnings("unchecked")
            Map<String, Object> yamlData = yaml.loadAs(reader, Map.class);
            if (yamlData == null) {
                throw new IOException("Failed to parse config file - empty or invalid YAML");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> serverApi = getMapSafely(yamlData, "server_api");
            updateApi = getBooleanSafely(serverApi, "auto_update", false);
            preferredVersion = getStringSafely(serverApi, "preferred_version", "latest");

            updatePluginNames = getBooleanSafely(yamlData, "update_plugin_names", false);

            @SuppressWarnings("unchecked")
            Map<String, Object> onLaunchData = getMapSafely(yamlData, "on_launch");
            @SuppressWarnings("unchecked")
            Map<String, Object> heapData = getMapSafely(onLaunchData, "heap");
            initialHeapSize = getIntegerSafely(heapData, "initial", 1024);
            maximumHeapSize = getIntegerSafely(heapData, "maximum", 2048);
            forceUnlockSessions = getBooleanSafely(onLaunchData, "force_unlock_sessions", false);

            @SuppressWarnings("unchecked")
            List<String> flagsList = getListSafely(onLaunchData, "flags");
            additionalFlags = flagsList.toArray(new String[0]);

            @SuppressWarnings("unchecked")
            Map<String, Object> logManagementData = getMapSafely(yamlData, "log_management");
            enableAutoDelete = getBooleanSafely(logManagementData, "enable_auto_delete", false);
            retentionDays = getIntegerSafely(logManagementData, "retention_days", 7);
        } catch (IOException e) {
            Logging.severe("Failed to load config file", e);
        }
    }

    private Map<String, Object> getMapSafely(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) value;
            return result;
        }
        return Collections.emptyMap();
    }

    private List<String> getListSafely(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) value;
            return result;
        }
        return Collections.emptyList();
    }

    private String getStringSafely(Map<String, Object> source, String key, String defaultValue) {
        Object value = source.get(key);
        return value instanceof String ? (String) value : defaultValue;
    }

    private boolean getBooleanSafely(Map<String, Object> source, String key, boolean defaultValue) {
        Object value = source.get(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    private int getIntegerSafely(Map<String, Object> source, String key, int defaultValue) {
        Object value = source.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private void copyConfigFile(File targetFile) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/config.yml");
             OutputStream outputStream = new FileOutputStream(targetFile)) {
            if (inputStream == null) {
                throw new IOException("Default config resource not found");
            }
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

    public String getPreferredVersion() {
        return preferredVersion;
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
        return additionalFlags.clone();
    }

    public boolean shouldForceUnlockSessions() {
        return forceUnlockSessions;
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

    public boolean isAutoDeleteEnabled() {
        return enableAutoDelete;
    }

    public int getRetentionDays() {
        return retentionDays;
    }
}
