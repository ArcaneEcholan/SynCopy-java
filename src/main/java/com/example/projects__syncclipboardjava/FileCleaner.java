package com.example.projects__syncclipboardjava;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;

public class FileCleaner {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    // yyyyMMdd'T'HHmmss -> epoch ms
    public static long formatToEpochMs(String timeStr) {
        LocalDateTime ldt = LocalDateTime.parse(timeStr, FORMATTER);
        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // epoch ms -> yyyyMMdd'T'HHmmss
    public static String epochMsToFormat(long epochMs) {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault());
        return ldt.format(FORMATTER);
    }

    // 清理所有 epoch < expireEpochMs 的文件
    public static void cleanOldFiles(String syncPath, long expireEpochMs) throws IOException {
        Path dir = Paths.get(syncPath);
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Not a directory: " + syncPath);
        }

        Files.list(dir).filter(Files::isRegularFile).forEach(path -> {
            String fileName = path.getFileName().toString();
            try {
                // 前 15 个字符为时间戳
                String timestamp = fileName.substring(0, 15);
                long fileEpoch = formatToEpochMs(timestamp);

                if (fileEpoch < expireEpochMs) {
                    Files.delete(path);
                    System.out.println("Deleted: " + fileName);
                }
            } catch (Exception e) {
                // 跳过无法解析的文件
            }
        });
    }

    public static void main(String[] args) throws IOException {
        long now = System.currentTimeMillis();
        long oneDayAgo = now - 24 * 60 * 60 * 1000L; // 1天前
        cleanOldFiles("/path/to/syncPath", oneDayAgo);
    }
}
