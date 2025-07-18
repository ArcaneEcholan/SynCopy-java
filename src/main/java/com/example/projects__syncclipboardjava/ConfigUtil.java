package com.example.projects__syncclipboardjava;

import java.nio.file.*;

public class ConfigUtil {
    public static Path getConfigPath(String appName) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            return Paths.get(System.getenv("APPDATA"), appName, "config.json");
        else
            return Paths.get(System.getProperty("user.home"), ".config", appName, "config.json");
    }

    public static Path getCachePath(String appName) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            return Paths.get(System.getenv("LOCALAPPDATA"), appName);
        else
            return Paths.get(System.getProperty("user.home"), ".cache", appName);
    }
}
