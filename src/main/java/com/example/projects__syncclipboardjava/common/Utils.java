package com.example.projects__syncclipboardjava.common;

import ch.qos.logback.core.util.*;
import cn.hutool.core.exceptions.*;
import com.fasterxml.jackson.databind.*;
import lombok.*;
import org.jetbrains.annotations.*;
import org.slf4j.helpers.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static com.example.projects__syncclipboardjava.common.SimpleFactories.*;

public class Utils {

    static ObjectMapper objectMapper = JsonUtil.getOm();

    @SneakyThrows
    public static String ok(@Nullable Object obj) {
        if (obj == null) {

            return objectMapper.writeValueAsString(ofJson("code", "SUCCESS"));
        }
        return objectMapper.writeValueAsString(ofJson("code", "SUCCESS", "data", obj));
    }

    @SneakyThrows
    public static String dsNotAvailable() {
        return objectMapper.writeValueAsString(ofJson("code", "DS_NOT_AVAILABLE"));
    }

    @SneakyThrows
    public static String langNotSupport() {
        return objectMapper.writeValueAsString(ofJson("code", "LANG_NOT_SUPPORT"));
    }

    @SneakyThrows
    public static String error() {
        return objectMapper.writeValueAsString(ofJson("code", "SERVER_ERROR"));
    }

    @NotNull
    public static String levelStr(Integer num) {
        if (num == null) {
            return "TRACE";
        }
        switch (num) {
        case 0:
            return "TRACE";
        case 1:
            return "DEBUG";
        case 2:
            return "INFO";
        case 3:
            return "WARNING";
        case 4:
            return "ERROR";
        default:
            return "TRACE";
        }
    }

    private static final SystemInfo oshiSystemInfo = new SystemInfo();

    public static SystemInfo oshiSystemInfo() {
        return oshiSystemInfo;
    }

    @NotNull
    public static <T> T nn(T notNull) {
        return Objects.requireNonNull(notNull);
    }

    // region: port checking
    public static void udpPortOk(String host, Integer port, RuntimeException ex) {
        boolean portAvailable = Utils.isUdpPortAvailable(host, port);
        if (!portAvailable) {
            throw ex;
        }
    }

    public static void udpPortOk(Integer port, RuntimeException ex) {
        boolean portAvailable = Utils.isUdpPortAvailable(port);
        if (!portAvailable) {
            throw ex;
        }
    }

    public static void tcpPortOk(Integer port, RuntimeException ex) {
        boolean portAvailable = Utils.isPortAvailable(port);
        if (!portAvailable) {
            throw ex;
        }
    }

    public static boolean isPortAvailable(Integer port) {
        try (var serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isUdpPortAvailable(Integer port) {
        try (var datagramSocket = new DatagramSocket(port)) {
            datagramSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isUdpPortAvailable(String host, Integer port) {
        try (var datagramSocket = new DatagramSocket(new InetSocketAddress(host, port))) {
            datagramSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    // endregion

    @NotNull
    public static String fmt(String format, Object... args) {
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }

    @NotNull
    public static Throwable getRootCause(@NotNull Throwable t) {
        var rt = ExceptionUtil.getRootCause(t);
        if (rt == null) {
            return t;
        }
        return rt;
    }

    public static String getRootMsg(@NotNull Throwable t) {
        return getRootCause(t).getMessage();
    }

    @Nullable
    public static Long longValue(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        return ((Number) obj).longValue();
    }

    @Nullable
    public static Double doubleValue(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        return ((Number) obj).doubleValue();
    }

    @Nullable
    public static Integer intValue(@Nullable String obj) {
        if (obj == null) {
            return null;
        }
        return Integer.valueOf(obj);
    }

    @Nullable
    public static Integer intValue(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        return ((Number) obj).intValue();
    }

    @Nullable
    public static String toUpperCase(@Nullable String obj) {
        if (obj == null) {
            return null;
        }
        return obj.toUpperCase();
    }

    @Nullable
    public static String toLowerCase(@Nullable String obj) {
        if (obj == null) {
            return null;
        }
        return obj.toLowerCase();
    }

    @NotNull
    public static <T> T withDefault(@Nullable T obj, T theDefault) {
        if (obj == null) {
            return theDefault;
        }
        return obj;
    }

    public static void setIfNotNull(@NotNull Map map, @NotNull String key, @Nullable Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    public interface Provider<T> {

        T run() throws Exception;
    }

    @Deprecated
    public static String getParentOfCaller() {
        return "";
    }

    @Deprecated
    public static String getSuperParentOfCaller() {
        return "";
    }

    public static boolean eq(Object a, Object b) {
        return Objects.equals(a, b);
    }

}
