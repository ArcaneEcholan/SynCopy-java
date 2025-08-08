package com.example.projects__syncclipboardjava;

import com.fasterxml.jackson.databind.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static com.example.projects__syncclipboardjava.FileCleaner.*;

@SpringBootApplication
public class ProjectsSyncclipboardJavaApplication {

    public static void main(String[] args) {
        // SpringApplication.run(ProjectsSyncclipboardJavaApplication.class, args);

        SpringApplicationBuilder builder = new SpringApplicationBuilder(ProjectsSyncclipboardJavaApplication.class);
        // https://stackoverflow.com/questions/51004447/spring-boot-java-awt-headlessexception
        builder.headless(false);

        ConfigurableApplicationContext context = builder.run(args);
    }

    @Slf4j
    @RestController
    public static class Controller {

        @GetMapping("/api/ping")
        public String ping() {
            log.info("pinged");
            return "pong";
        }
    }

    @Slf4j
    @Component
    public static class RunnerVersion1 implements SmartInitializingSingleton {
        void abc() {
        }

        @Override
        public void afterSingletonsInstantiated() {
            try {
                log.info("Java version: " + System.getProperty("java.version"));

                String appName = "SynCopy";
                Path configPath = ConfigUtil.getConfigPath(appName);
                if (!Files.exists(configPath)) {

                    Files.createDirectories(configPath.getParent());

                    Files.write(configPath, "{}".getBytes(StandardCharsets.UTF_8));
                }

                log.info("Loading config from " + configPath);
                String json = new String(Files.readAllBytes(configPath), StandardCharsets.UTF_8);
                Map<String, Object> cfg = new ObjectMapper().readValue(json, Map.class);
                String syncDir = (String) cfg.get("sync_dir");
                if (syncDir == null || syncDir.trim().isEmpty()) {
                    log.info("sync_dir is required");
                    System.exit(1);
                }
                Path syncPath = Paths.get(syncDir, "items");
                Files.createDirectories(syncPath);

                Path cacheDir = ConfigUtil.getCachePath(appName);
                Files.createDirectories(cacheDir);
                Path appliedRecord = cacheDir.resolve("last_applied.txt");

                SharedState sharedState = new SharedState();
                Runnable cbMonitor = () -> {
                    try {
                        String content = ClipboardUtil.get();
                        if (content == null || content.trim().isEmpty()) {
                            return;
                        }
                        String hash = FileUtil.md5(content);

                        if (!hash.equals(sharedState.lastSeenHash)) {
                            String fname = FileUtil.generateFileName();
                            Path out = syncPath.resolve(fname);
                            Files.write(out, content.getBytes(StandardCharsets.UTF_8));
                            Files.write(appliedRecord, fname.getBytes(StandardCharsets.UTF_8));
                            sharedState.lastSeenHash = hash;
                            log.info("clipboard changed, wrote " + fname);
                        }
                    } catch (Exception e) {
                        log.error("ClipboardMonitor error: " + e);
                        e.printStackTrace();
                    }

                };
                Runnable cbUpdator = () -> {
                    try {
                        String lastApplied = Files.exists(appliedRecord)
                                ? new String(Files.readAllBytes(appliedRecord)).trim() : "";

                        List<Path> files;
                        try (Stream<Path> stream = Files.list(syncPath)) {
                            files = stream.filter(p -> p.toString().endsWith(".txt")).sorted(Comparator.reverseOrder())
                                    .collect(Collectors.toList());
                        }

                        var file = files.stream().findFirst().orElse(null);
                        if (file == null) {
                            return;
                        }
                        if (file.getFileName().toString().equals(lastApplied)) {
                            return;
                        }

                        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                        ClipboardUtil.set(content);
                        Files.write(appliedRecord, file.getFileName().toString().getBytes(StandardCharsets.UTF_8));
                        sharedState.lastSeenHash = FileUtil.md5(content);
                        log.info("==> updated clipboard from " + file.getFileName());
                    } catch (Exception e) {
                        log.error("ClipboardMonitor error: " + e);
                        e.printStackTrace();
                    }

                };
                RepeatedTask.taskFireUp().topic("ClipboardSyncer").interval(500).task(() -> {
                    cbMonitor.run();
                    cbUpdator.run();
                });
                RepeatedTask.taskFireUp().topic("appechoer").interval(3000).task(() -> {
                    log.info("app thread echo");
                });
                RepeatedTask.taskFireUp().topic("Cleaner").interval(3600 * 1000).task(() -> {
                    long now = System.currentTimeMillis();
                    long expire = now - 1 * 60 * 60 * 1000L;
                    try {
                        cleanOldFiles(syncPath.toString(), expire);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * @Component public static class Runner implements CommandLineRunner {
     *
     * @Override public void run(String... args) throws Exception { SynCopy.main(args); } }
     */
}
