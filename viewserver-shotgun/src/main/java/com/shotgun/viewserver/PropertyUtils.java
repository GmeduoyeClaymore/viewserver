package com.shotgun.viewserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils{
    public static void loadProperties(String environment){
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties props = new Properties();
            try (InputStream resourceStream = loader.getResourceAsStream(String.format("%s.properties", environment))) {
                props.load(resourceStream);
                for (String propName : props.stringPropertyNames()) {
                    System.setProperty(propName, (String) props.get(propName));
                }
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
