package me.coopersully.vilicus;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class PluginRenamer {

    public static void renamePlugins() {
        System.out.println("Starting plugin renaming process...");

        File pluginsDir = new File("plugins");
        if (!pluginsDir.exists()) {
            System.out.println("Plugin renaming failed; the plugins directory doesn't exist.");
            return;
        }

        File[] jarFiles = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) {
            System.out.println("Plugin renaming failed; couldn't find any jar files in " + pluginsDir.getAbsolutePath());
            return;
        }

        System.out.println("Found " + jarFiles.length + " jar files in the plugins directory.");
        for (File file : jarFiles) {
            renameFile(file);
        }
        System.out.println("Finished renaming all jar files in the plugins directory.");
    }

    private static void renameFile(File file) {
        try (JarFile jar = new JarFile(file)) {
            ZipEntry entry = jar.getEntry("plugin.yml");
            if (entry != null) {
                String ymlContent = new String(jar.getInputStream(entry).readAllBytes());
                String name = parseValue(ymlContent, "name");
                String version = parseValue(ymlContent, "version");
                String newName = name + "-" + version + ".jar";

                if (!file.getName().equals(newName)) {
                    if (file.renameTo(new File("plugins", newName))) {
                        System.out.println("Renamed " + file.getName() + " to " + newName);
                    } else {
                        System.out.println("Failed to rename " + file.getName());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error renaming " + file.getName() + ": " + e.getMessage());
        }
    }

    private static @NotNull String parseValue(@NotNull String ymlContent, @NotNull String key) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(ymlContent);

        Object value = map.get(key);
        if (value != null) {
            return value.toString().trim();
        } else {
            throw new IllegalArgumentException("Key not found in YML content: " + key);
        }
    }
}
