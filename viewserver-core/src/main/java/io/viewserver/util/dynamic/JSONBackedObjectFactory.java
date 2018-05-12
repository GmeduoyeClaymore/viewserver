package io.viewserver.util.dynamic;

import io.viewserver.controller.ControllerUtils;

import java.util.HashMap;
import java.util.Map;

import static io.viewserver.util.dynamic.MethodInterpreters.caching;

public class JSONBackedObjectFactory {
    public static <T> T create(Class<T> classToCreate) {
        return Proxies.propertyMapping(classToCreate, new HashMap<>());
    }

    public static <T> T create(String proxyString, Class<T> classToCreate) {
        return Proxies.propertyMapping(classToCreate, (Map<String, Object>) ControllerUtils.mapDefault(proxyString));
    }

    public static <T> T create(Map<String, Object> propertyValues, Class<T> classToCreate) {
        PropertyValueStore store = new PropertyValueStore(classToCreate, propertyValues);
        return Proxies.simpleProxy(classToCreate, store.createMethodInterpreter(), EqualisableByState.class, DynamicJsonBackedObject.class);
    }
}


