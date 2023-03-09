package me.coopersully.vilicus;

import java.io.File;

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

    }

}