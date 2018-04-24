package io.viewserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.viewserver.core.JacksonSerialiser;
import org.apache.commons.collections.map.ListOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        return JacksonSerialiser.getInstance().serialise(ser);
    }

    public static String toConsistentString(Object ser) {
        if (ser == null) {
            return null;
        }
        if(ser instanceof String){
            return (String) ser;
        }
        if(ser instanceof HashMap){
            ser = convertToListMapAndOrderProperties((HashMap)ser);
        }
        return JacksonSerialiser.getInstance().serialise(ser);
    }


    private static Map convertToListMapAndOrderProperties(HashMap ser) {
        ListOrderedMap listmap = new ListOrderedMap();
        ArrayList<String> keys = new ArrayList<String>(ser.keySet());
        keys.sort(String::compareTo);
        int propCounter = 0;
        for(String key : keys){
            Object element = ser.get(key);
            if(element instanceof HashMap){
                element = convertToListMapAndOrderProperties((HashMap) element);
            }
            listmap.put(propCounter++, key,element);
        }
        return listmap;
    }
}
