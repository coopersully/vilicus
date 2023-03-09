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

        // get the plugins directory
        File pluginsDir = new File("plugins");

        // check if the directory exists
        if (!pluginsDir.exists()) {
            System.out.println("Plugin renaming failed; the plugins directory doesn't exist.");
            return;
        }

        // get a list of all jar files in the plugins directory
        File[] jarFiles = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) {
            // handle the case where pluginsDir is not a directory or doesn't exist
            System.out.println("Plugin renaming failed; couldn't find any jar files in " + pluginsDir.getAbsolutePath());
            return;
        }

        System.out.println("Found " + jarFiles.length + " jar files in the plugins directory.");

        // iterate over all jar files in the plugins directory
        for (File file : jarFiles) {
            try {
                // open the jar file
                JarFile jar = new JarFile(file);

                // get the plugin.yml entry
                ZipEntry entry = jar.getEntry("plugin.yml");
                if (entry != null) {
                    // read the plugin.yml file
                    String ymlContent = new String(jar.getInputStream(entry).readAllBytes());

                    // parse the name and version from the ymlContent
                    String name = parseValue(ymlContent, "name");
                    String version = parseValue(ymlContent, "version");

                    // rename the file to NAME-VERSION.jar
                    String newName = name + "-" + version + ".jar";

                    if (!file.getName().equals(newName)) {
                        file.renameTo(new File("plugins/" + newName));
                        System.out.println("Renamed " + file.getName() + " to " + newName);
                    }
                }

                // close the jar file
                jar.close();
            } catch (IOException e) {
                // handle any errors that occur while reading the jar file
                System.out.println("Error renaming " + file.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("Finished renaming all jar files in the plugins directory.");
    }

    private static @NotNull String parseValue(@NotNull String ymlContent, @NotNull String key) {
        // create a new YAML parser
        Yaml yaml = new Yaml();

        // parse the YML content into a Map
        Map<String, Object> map = yaml.load(ymlContent);

        // extract the value of the key from the Map
        Object value = map.get(key);

        // check if the key is present in the Map
        if (value != null) {
            // return the value as a trimmed string
            return value.toString().trim();
        } else {
            // handle the case where the key is not found in the YML content
            throw new IllegalArgumentException("Key not found in YML content: " + key);
        }
    }


}
