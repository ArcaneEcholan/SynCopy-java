package com.example.projects__syncclipboardjava;

import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

public class SynCopy {

    private static final Logger logger = Logger.getLogger("SynCopy");

    public static void main(String[] args) throws Exception {
        setupLogger();

        logger.info("Java version: " + System.getProperty("java.version"));

        String appName = "SynCopy";
        Path configPath = ConfigUtil.getConfigPath(appName);
        if (!Files.exists(configPath)) {
            Files.createDirectories(configPath.getParent());
            Files.write(configPath, "{}".getBytes(StandardCharsets.UTF_8));
        }

        logger.info("Loading config from " + configPath);
        String json = new String(Files.readAllBytes(configPath), StandardCharsets.UTF_8);
        Map<String, Object> cfg = new ObjectMapper().readValue(json, Map.class);
        String syncDir = (String) cfg.get("sync_dir");
        if (syncDir == null || syncDir.trim().isEmpty()) {
            logger.info("sync_dir is required");
            System.exit(1);
        }
        Path syncPath = Paths.get(syncDir, "items");
        Files.createDirectories(syncPath);

        Path cacheDir = ConfigUtil.getCachePath(appName);
        Files.createDirectories(cacheDir);
        Path appliedRecord = cacheDir.resolve("last_applied.txt");

        SharedState sharedState = new SharedState();

        new Thread(new ClipboardMonitor(syncPath, appliedRecord, sharedState), "ClipboardMonitor").start();
        new Thread(new ClipboardUpdater(syncPath, appliedRecord, sharedState), "ClipboardUpdater").start();

        while (true)
            Thread.sleep(60_000);
    }

    private static void setupLogger() throws IOException {
        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers())
            root.removeHandler(h);

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.INFO);
        ch.setFormatter(new SimpleFormatter());
        root.addHandler(ch);

        FileHandler fh = new FileHandler("app.log", 10 * 1024 * 1024, 5, true);
        fh.setLevel(Level.FINE);
        fh.setFormatter(new SimpleFormatter());
        root.addHandler(fh);

        root.setLevel(Level.FINE);
    }
}
