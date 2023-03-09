package me.coopersully.vilicus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ServerUpdater {

    private static final String SERVER_DOWNLOADS_URL = "https://purpurmc.org/downloads?v=";
    private static final String DEFAULT_FILE_NAME = "server.jar";
    private static final String HISTORY_FILE_NAME = "vilicus/.server_api_version";

    public static void updateAPI(String[] args) throws Exception {

        /* Retrieve and print the server's currently-installed
        api version from the history file, or handle it if there
        is not a version in recent history. */
        String currentVersion = getCurrentVersion();
        if (currentVersion == null) {
            System.out.println("SERVER VERSION - UNKNOWN");
        } else {
            System.out.println("SERVER VERSION - " + currentVersion + " (" + getPrettyVersion(currentVersion) + ")");
        }

        /* Retrieve and print the latest api version from Purpur's
        website, or crash if it could not be fetched for some reason. */
        String latestVersion = getLatestVersion();
        if (latestVersion == null) {
            System.out.println("LATEST VERSION - NONE");
            return;
        } else {
            System.out.println("LATEST VERSION - " + latestVersion + " (" + getPrettyVersion(latestVersion) + ")");
        }

        // Already have the latest version? That's all folks!
        if (latestVersion.equals(currentVersion)) {
            System.out.println("We're already on the latest version!");
            return;
        }

        // Download the latest api version from Purpur
        System.out.println("Downloading & installing latest version...");
        File file = getFilePath(args.length > 0 ? args[0] : DEFAULT_FILE_NAME);
        saveURLtoFile(new URL(latestVersion), file);
    }

    private static File getFilePath(String fileName) {
        File file = null;

        try {
            File currentDir = (new File(ServerUpdater.class.getProtectionDomain().getCodeSource().getLocation().toURI())).getParentFile();
            file = new File(currentDir, fileName);
        } catch (URISyntaxException var3) {
            var3.printStackTrace();
        }

        return file;
    }

    private static String getCurrentVersion() {
        String currentVersion = null;
        File historyFile = getFilePath(HISTORY_FILE_NAME);

        try {
            if (historyFile.exists()) {
                currentVersion = Files.readAllLines(historyFile.toPath()).get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return currentVersion;
    }

    private static @Nullable String getLatestVersion() {

        try {
            // Connect to Purpur's downloads page
            Connection conn = Jsoup.connect(SERVER_DOWNLOADS_URL);
            conn.timeout(3000);

            Document doc = conn.get();

            Element table = doc.select("table.downloads").first();
            assert table != null;

            Elements links = table.getElementsByTag("a");

            for (Element link : links) {
                String href = link.attr("href");
                if (href.endsWith("download")) return href;
            }

        } catch (IOException ignored) { }
        throw new RuntimeException("Failed to fetch downloads page from Purpur; are you connected to the internet?");

    }

    private static void saveURLtoFile(URL url, File file) {

        if (url == null || file == null) {
            System.out.println("Failed to save URL to a file.");
            return;
        }

        List<String> lines;
        try {

            Throwable t = null;
            try {
                InputStream in = url.openStream();

                try {
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    if (in != null) in.close();
                }

            } catch (Throwable t2) {
                if (t == null) t = t2;
                else if (t != t2) t.addSuppressed(t2);
                throw t;
            }
        } catch (Throwable t3) {
            t3.printStackTrace();
        }

        File logFile = getFilePath(HISTORY_FILE_NAME);
        lines = new ArrayList<>();
        lines.add(url.toString());

        try {
            Files.write(logFile.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String getPrettyVersion(@NotNull String url) {
        String[] portions = url.split("/");
        return "Minecraft " + portions[5] + ", API " + portions[6];
    }

}