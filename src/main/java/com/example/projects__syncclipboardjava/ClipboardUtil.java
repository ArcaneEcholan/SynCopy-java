package com.example.projects__syncclipboardjava;

import lombok.*;
import lombok.extern.slf4j.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.concurrent.*;

@Slf4j
public class ClipboardUtil {
    public static void set(String str) throws Exception {
        TimeoutOperationSync tos = new TimeoutOperationSync();

        try {
            // 有返回值
            String result = tos.runWithTimeout(() -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), null);
                return "done";
            }, 1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("set clipboard timeout");
            throw e;
        }

    }

    public static String get() throws Exception {
        TimeoutOperationSync tos = new TimeoutOperationSync();

        try {
            // 有返回值
            String result = tos.runWithTimeout(() -> {
                return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            }, 1, TimeUnit.SECONDS);
            return result;
        } catch (TimeoutException e) {
            log.error("get clipboard timeout");
            throw e;
        }

    }
}
