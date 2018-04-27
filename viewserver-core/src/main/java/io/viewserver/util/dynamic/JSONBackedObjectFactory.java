package io.viewserver.util.dynamic;

import io.viewserver.controller.ControllerUtils;

import java.util.HashMap;

import static io.viewserver.util.dynamic.MethodInterpreters.caching;

public class JSONBackedObjectFactory {
    public static <T> T create(Class<T> classToCreate) {
        return Proxies.propertyMapping(classToCreate, new HashMap<>());
    }

    public static <T> T create(String orderDetailsString, Class<T> classToCreate) {
        return Proxies.propertyMapping(classToCreate, ControllerUtils.mapDefault(orderDetailsString));
    }
}


