package com.example.projects__syncclipboardjava;

import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

public class ClipboardUpdater implements Runnable {
    private final Path syncDir;
    private final Path appliedRecord;
    private final SharedState sharedState;

    public ClipboardUpdater(Path syncDir, Path appliedRecord, SharedState sharedState) {
        this.syncDir = syncDir;
        this.appliedRecord = appliedRecord;
        this.sharedState = sharedState;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
                String lastApplied = Files.exists(appliedRecord) ? new String(Files.readAllBytes(appliedRecord)).trim()
                        : "";

                List<Path> files;
                try (Stream<Path> stream = Files.list(syncDir)) {
                    files = stream.filter(p -> p.toString().endsWith(".txt")).sorted(Comparator.reverseOrder())
                            .collect(Collectors.toList());
                }

                for (Path file : files) {
                    if (file.getFileName().toString().equals(lastApplied))
                        break;

                    String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                    ClipboardUtil.set(content);
                    Files.write(appliedRecord, file.getFileName().toString().getBytes(StandardCharsets.UTF_8));

                    synchronized (sharedState.lock) {
                        sharedState.lastSeenHash = FileUtil.md5(content);
                    }
                    Logger.getLogger("SynCopy").info("==> updated clipboard from " + file.getFileName());
                    break;
                }

            } catch (Exception e) {
                Logger.getLogger("SynCopy").severe("ClipboardUpdater error: " + e);
            }
        }
    }
}
