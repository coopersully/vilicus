package me.coopersully.vilicus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EulaAgreementManager {
    private static final String EULA_FILE = "eula.txt";

    public static void ensureEulaAgreement() {
        File eulaFile = new File(EULA_FILE);
        if (eulaFile.exists()) {
            Logging.info("EULA agreement is up to date.");
            return;
        }

        Logging.info("Updating EULA agreement file...");
        try (FileWriter writer = new FileWriter(eulaFile)) {
            writer.write("eula=true\n");
            Logging.info("EULA agreement has been updated.");
        } catch (IOException e) {
            Logging.severe("Failed to create or update the EULA agreement file", e);
        }
    }
}
