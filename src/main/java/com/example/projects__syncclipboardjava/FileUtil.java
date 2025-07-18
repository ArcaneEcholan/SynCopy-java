package com.example.projects__syncclipboardjava;

import java.nio.charset.*;
import java.security.*;
import java.time.*;
import java.time.format.*;

public class FileUtil {
    public static String generateFileName() {
        LocalDateTime now = LocalDateTime.now();
        String ts = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
        long ns = System.nanoTime() % 1_000_000_000;
        return ts + "_" + String.format("%09d", ns) + ".txt";
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b)); // 小写hex
        }
        return sb.toString();
    }

    public static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
