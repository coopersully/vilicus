package me.coopersully.vilicus;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PluginRenamer {
    private static final String PLUGINS_DIRECTORY = "plugins";

    public static void renamePlugins() {
        Logging.info("Starting plugin renaming process...");

        File pluginsDir = new File(PLUGINS_DIRECTORY);
        if (!pluginsDir.exists()) {
            Logging.warning("Plugin renaming failed; the plugins directory doesn't exist.");
            return;
        }

        File[] jarFiles = pluginsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            Logging.warning("Plugin renaming failed; couldn't find any jar files in " + pluginsDir.getAbsolutePath());
            return;
        }

        Logging.info("Found " + jarFiles.length + " jar files in the plugins directory.");

        for (File file : jarFiles) {
            try {
                String newName = getPluginFileName(file);
                if (newName != null && !file.getName().equals(newName)) {
                    File newFile = new File(pluginsDir, newName);
                    if (file.renameTo(newFile)) {
                        Logging.info("Renamed " + file.getName() + " to " + newName);
                    } else {
                        Logging.warning("Failed to rename " + file.getName());
                    }
                }
            } catch (IOException e) {
                Logging.severe("Error renaming " + file.getName(), e);
            }
        }

        Logging.info("Finished renaming all jar files in the plugins directory.");
    }

    private static String getPluginFileName(File jarFile) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return null;
            }

            String name = manifest.getMainAttributes().getValue("Name");
            String version = manifest.getMainAttributes().getValue("Version");

            if (name == null || version == null) {
                return null;
            }

            return name + "-" + version + ".jar";
        }
    }
}
