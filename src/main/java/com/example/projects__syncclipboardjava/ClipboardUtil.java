package com.example.projects__syncclipboardjava;

import java.awt.*;
import java.awt.datatransfer.*;

public class ClipboardUtil {
    public static void set(String str) throws Exception {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), null);
    }

    public static String get() throws Exception {
        return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    }
}
