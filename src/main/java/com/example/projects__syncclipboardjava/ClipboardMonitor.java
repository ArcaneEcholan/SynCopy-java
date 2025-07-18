package com.example.projects__syncclipboardjava;

import java.nio.charset.*;
import java.nio.file.*;
import java.util.logging.*;

public class ClipboardMonitor implements Runnable {
    private final Path syncDir;
    private final Path appliedRecord;
    private final SharedState sharedState;

    public ClipboardMonitor(Path syncDir, Path appliedRecord, SharedState sharedState) {
        this.syncDir = syncDir;
        this.appliedRecord = appliedRecord;
        this.sharedState = sharedState;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
                String content = ClipboardUtil.get();
                if (content == null || content.trim().isEmpty())
                    continue;
                String hash = FileUtil.md5(content);

                synchronized (sharedState.lock) {
                    if (!hash.equals(sharedState.lastSeenHash)) {
                        String fname = FileUtil.generateFileName();
                        Path out = syncDir.resolve(fname);
                        Files.write(out, content.getBytes(StandardCharsets.UTF_8));
                        Files.write(appliedRecord, fname.getBytes(StandardCharsets.UTF_8));
                        sharedState.lastSeenHash = hash;
                        Logger.getLogger("SynCopy").info("clipboard changed, wrote " + fname);
                    }
                }
            } catch (Exception e) {
                Logger.getLogger("SynCopy").severe("ClipboardMonitor error: " + e);
            }
        }
    }
}
