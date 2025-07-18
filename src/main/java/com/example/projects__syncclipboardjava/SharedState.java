package com.example.projects__syncclipboardjava;

public class SharedState {
    public final Object lock = new Object();
    public String lastSeenHash = "";
}
