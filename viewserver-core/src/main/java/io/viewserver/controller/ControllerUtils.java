package io.viewserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class ControllerUtils {

    private static TypeReference<HashMap<String, Object>> dictionaryType = new TypeReference<HashMap<String, Object>>() {
    };
    private static ObjectMapper mapper = new ObjectMapper();

    public static HashMap<String, Object> mapDefault(String json) {
        HashMap<String, Object> map = null;
        try {
            map = mapper.readValue(json, dictionaryType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static String toString(Object ser) {
        if (ser == null) {
            return null;
        }
        try {

            return mapper.writerFor(ser.getClass()).writeValueAsString(ser);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize object \"" + ser + "\"", e);
        }
    }
}
