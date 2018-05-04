package io.viewserver.server.steps;

import io.viewserver.controller.ControllerUtils;
import org.h2.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {
    public static String getJsonStringFromFile(String dataFile) {
        InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream(dataFile);
        if (inputStream == null) {
            throw new RuntimeException("Unable to find resource at at " + dataFile);
        }
        Reader reader = IOUtils.getBufferedReader(inputStream);
        try {
            return parseReferencesInJSONFile(IOUtils.readStringAndClose(reader, 0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String parseReferencesInJSONFile(String json) {
        HashMap<String, Object> result = (HashMap<String, Object>) ControllerUtils.mapDefault(json);
        replaceReferences(result);
        return ControllerUtils.toString(result);
    }


    public static Object getJsonObjectFromFile(String dataFile) {
        String jsonStringFromFile = getJsonStringFromFile(dataFile);
        return ControllerUtils.mapDefault(jsonStringFromFile);
    }

    public static String replaceFileReferences(String value) {
        if(value == null){
            return null;
        }
        if (value.startsWith("ref://")) {
            String fileReference = value.substring(6);
            value = getJsonStringFromFile(fileReference);
        }
        return value;
    }


    public static void replaceReferences(HashMap<String, Object> result) {
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            if (entry.getValue() instanceof HashMap) {
                replaceReferences((HashMap) entry.getValue());
            } else if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                entry.setValue(replaceReference(value));
            }
        }
    }

    public static Object replaceReference(String value){
        if (value.startsWith("ref://")) {
            String fileReference = value.substring(6);
            return getJsonStringFromFile(fileReference);
        }
        if (value.startsWith("objref://")) {
            String fileReference = value.substring(9);
            return getJsonObjectFromFile(fileReference);
        }

        return  value;
    }
}
