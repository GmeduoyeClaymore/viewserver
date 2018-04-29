package io.viewserver.util.dynamic;

import io.viewserver.controller.ControllerUtils;

import java.util.HashMap;
import java.util.Map;

import static io.viewserver.util.dynamic.MethodInterpreters.caching;

public class JSONBackedObjectFactory {
    public static <T> T create(Class<T> classToCreate) {
        return Proxies.propertyMapping(classToCreate, new HashMap<>());
    }

    public static <T> T create(String orderDetailsString, Class<T> classToCreate) {
        return Proxies.propertyMapping(classToCreate, ControllerUtils.mapDefault(orderDetailsString));
    }

    public static <T> T create(Map<String, Object> propertyValues, Class<T> classToCreate) {
        PropertyValueStore store = new PropertyValueStore(classToCreate, propertyValues);
        return Proxies.simpleProxy(classToCreate, store.createMethodInterpreter(), EqualisableByState.class, DynamicJsonBackedObject.class);
    }
}


