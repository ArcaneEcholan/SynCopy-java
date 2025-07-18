package com.example.projects__syncclipboardjava.common;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.*;
import com.fasterxml.jackson.datatype.jsr310.*;
import lombok.*;
import org.slf4j.*;

import javax.validation.constraints.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.*;

public class JsonUtil {

    @Getter
    static final ObjectMapper om;

    @org.jetbrains.annotations.NotNull
    private static final Logger log = Objects.requireNonNull(LoggerFactory.getLogger(JsonUtil.class));

    static {
        // don't use spring, it'll mess up with the default ObjectMapper
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        om = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return getOm().convertValue(fromValue, toValueType);
    }

    @SneakyThrows
    @NotNull
    public static String toJsonString(Object obj) {
        try {
            return om.writeValueAsString(obj);
        } catch (InvalidDefinitionException e) {
            throw new RuntimeException("properties of the object must has getter to be serialized: " + e.getMessage(),
                    e);
        }
    }

    public static Map parse(String jsonString) {
        return parse(jsonString, Map.class);
    }

    public static <T> T parse(String jsonString, Class<T> clazz) {
        try {
            return om.readValue(jsonString, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON_FORMAT_ERROR_MSG", e);
        }
    }

    public static <T> T parseObject(String jsonString, Class<T> clazz) {
        return parse(jsonString, clazz);
    }

    public static Map parseObject(String jsonString) {
        return parse(jsonString);
    }

    public static Map objToJsonObj(Object json) {
        return parse(toJsonString(json));
    }

    public static <T> T objToJsonObj(Object json, Class<T> clazz) {
        return parse(toJsonString(json), clazz);
    }

    @SneakyThrows
    public static <T> List<T> parseList(String json, Class<T> clazz) {
        return om.readValue(json, om.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    @SneakyThrows
    public static List parseList(String json) {
        return om.readValue(json, List.class);
    }

    // v2: throw exception explicitly
    @NotNull
    public static String toJsonStringV2(Object obj) throws JsonProcessingException {
        return om.writeValueAsString(obj);
    }

    public static Map parseV2(String jsonString) throws JsonProcessingException {
        return parseV2(jsonString, Map.class);
    }

    public static <T> T parseV2(String jsonString, Class<T> clazz) throws JsonProcessingException {
        return om.readValue(jsonString, clazz);
    }

    public static <T> T parseObjectV2(String jsonString, Class<T> clazz) throws JsonProcessingException {
        return parseV2(jsonString, clazz);
    }

    public static Map parseObjectV2(String jsonString) throws JsonProcessingException {
        return parseV2(jsonString);
    }

    public static Map objToJsonObjV2(Object json) throws JsonProcessingException {
        return parseV2(toJsonStringV2(json));
    }

    public static <T> T objToJsonObjV2(Object json, Class<T> clazz) throws JsonProcessingException {
        return parseV2(toJsonStringV2(json), clazz);
    }

    public static <T> List<T> parseListV2(String json, Class<T> clazz) throws JsonProcessingException {
        return om.readValue(json, om.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static List parseListV2(String json) throws JsonProcessingException {
        return om.readValue(json, List.class);
    }

    @Deprecated
    public static Object jsonPathRead(String json, String path) {
        try {
            // Ensure the path starts with "$"
            if (!path.startsWith("$")) {
                throw new IllegalArgumentException("Path must start with '$'");
            }

            // Parse the JSON string into a JsonNode
            JsonNode currentNode = om.readTree(json);

            // Handle the special case where path is just "$"
            if ("$".equals(path)) {
                return convertNode(currentNode, null);
            }

            // Use a regex-based approach to parse the path
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_-]+|\\[\\d+])");
            Matcher matcher = pattern.matcher(path.substring(1));

            while (matcher.find()) {
                String key = matcher.group();

                // Handle array indexing
                if (key.startsWith("[")) {
                    // Extract the index from the array notation
                    int index = Integer.parseInt(key.substring(1, key.length() - 1));
                    if (currentNode != null && currentNode.isArray()) {
                        currentNode = currentNode.get(index);
                    } else {
                        throw new RuntimeException("The path does not point to a valid json array: " + key);
                    }
                } else {
                    if (currentNode == null) {
                        return null;
                    }
                    // Navigate to the next node using the key
                    currentNode = currentNode.get(key);
                }
            }

            // Convert the final node to the desired type
            return convertNode(currentNode, null);
        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON path: " + path + " : " + e.getMessage(), e);
        }
    }

    /**
     * Too complex to maintain, stop using!!
     */
    @Deprecated
    public static <T> T jsonPathRead(String json, String path, Class<T> clazz) {
        try {
            // Ensure the path starts with "$"
            if (!path.startsWith("$")) {
                throw new IllegalArgumentException("Path must start with '$'");
            }

            // Parse the JSON string into a JsonNode
            JsonNode currentNode = om.readTree(json);

            // Handle the special case where path is just "$"
            if ("$".equals(path)) {
                return convertNode(currentNode, clazz);
            }

            // Use a regex-based approach to parse the path
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_-]+|\\[\\d+])");
            Matcher matcher = pattern.matcher(path.substring(1));

            while (matcher.find()) {
                String key = matcher.group();

                // Handle array indexing
                if (key.startsWith("[")) {
                    // Extract the index from the array notation
                    int index = Integer.parseInt(key.substring(1, key.length() - 1));
                    if (currentNode != null && currentNode.isArray()) {
                        currentNode = currentNode.get(index);
                    } else {
                        throw new RuntimeException("The path does not point to a valid json array: " + key);
                    }
                } else {
                    if (currentNode == null) {
                        return null;
                    }
                    // Navigate to the next node using the key
                    currentNode = currentNode.get(key);
                }
            }

            // Convert the final node to the desired type
            return convertNode(currentNode, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON path: " + path + " : " + e.getMessage(), e);
        }
    }

    /**
     * Too complex to maintain, stop using!!
     */
    @Deprecated
    public static List jsonPathReadList(String json, String path) {
        return (List) jsonPathRead(json, path);
    }

    /**
     * Too complex to maintain, stop using!!
     */
    @Deprecated
    public static <T> List<T> jsonPathReadList(String json, String path, Class<T> clazz) {
        try {
            // Ensure the path starts with "$"
            if (!path.startsWith("$")) {
                throw new IllegalArgumentException("Path must start with '$'");
            }

            // Parse the JSON string into a JsonNode
            JsonNode currentNode = om.readTree(json);

            // Handle the special case where path is just "$"
            if ("$".equals(path)) {
                return (List<T>) convertNode(currentNode, clazz);
            }

            // Use a regex-based approach to parse the path
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_-]+|\\[\\d+])");
            Matcher matcher = pattern.matcher(path.substring(1));

            while (matcher.find()) {
                String key = matcher.group();

                // Handle array indexing
                if (key.startsWith("[")) {
                    // Extract the index from the array notation
                    int index = Integer.parseInt(key.substring(1, key.length() - 1));
                    if (currentNode != null && currentNode.isArray()) {
                        currentNode = currentNode.get(index);
                    } else {
                        throw new RuntimeException("The path does not point to a valid json array: " + key);
                    }
                } else {
                    if (currentNode == null) {
                        return null;
                    }
                    // Navigate to the next node using the key
                    currentNode = currentNode.get(key);
                }
            }

            // Convert the final node to the desired type
            return (List<T>) convertNode(currentNode, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON path: " + path + " : " + e.getMessage(), e);
        }
    }

    /**
     * Too complex to maintain, stop using!!
     */
    @Deprecated
    private static <T> T convertNode(JsonNode node, Class<T> clazz) {
        if (node == null) {
            return null;
        }
        if (node.isArray()) {
            if (clazz == null) {
                return (T) om.convertValue(node, List.class);
            }
            return om.convertValue(node, om.getTypeFactory().constructCollectionType(List.class, clazz));
        } else {
            if (clazz == null) {
                return (T) om.convertValue(node, Object.class);
            }
            return om.convertValue(node, clazz);
        }
    }
}
