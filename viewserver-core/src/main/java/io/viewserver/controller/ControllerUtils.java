package io.viewserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import org.apache.commons.collections.map.ListOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerUtils {

    private static TypeReference<HashMap<String, Object>> dictionaryType = new TypeReference<HashMap<String, Object>>() {
    };
    private static TypeReference<List<HashMap>> listDictionaryType = new TypeReference<List<HashMap>>(){

    };
    private static ObjectMapper mapper = new ObjectMapper();

    public static Object mapDefault(String json) {
        if(json.startsWith("[")){
            try {
                return mapper.readValue(json, listDictionaryType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        if(ser instanceof String){
            return (String) ser;
        }

        if(ser.getClass().isAssignableFrom(DynamicJsonBackedObject.class)){
            return ((DynamicJsonBackedObject)ser).serialize();
        }

        if (ser.getClass().isEnum()){
            return ((Enum)ser).name();
        }

        return JacksonSerialiser.getInstance().serialise(ser);

    }

    public static String toConsistentString(Object ser, String propertyName) {
        if (ser == null) {
            return null;
        }
        if(ser instanceof String){
            return (String) ser;
        }
        if(ser instanceof HashMap){
            if(propertyName !=null){
                ser = ((HashMap)ser).get(propertyName);
            }else{
                ser = convertToListMapAndOrderProperties((HashMap)ser);
            }
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
