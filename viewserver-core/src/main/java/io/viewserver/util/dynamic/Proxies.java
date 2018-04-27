package io.viewserver.util.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.stream.Stream;

import static io.viewserver.util.dynamic.MethodInterpreters.caching;
import static io.viewserver.util.dynamic.MethodInterpreters.handlingDefaultMethods;
import static io.viewserver.util.dynamic.MethodInterpreters.intercepting;

public final class Proxies {

    @SuppressWarnings("unchecked")
    public static <T> T simpleProxy(Class<? extends T> iface, InvocationHandler handler, Class<?>...otherIfaces) {
        Class<?>[] allInterfaces = Stream.concat(
                Stream.of(iface),
                Stream.of(otherIfaces))
                .distinct()
                .toArray(Class<?>[]::new);

        return (T) Proxy.newProxyInstance(iface.getClassLoader(),
                allInterfaces,
                handler);
    }


    public static <T> T propertyMapping(Class<? extends T> iface, Map<String, Object> propertyValues) {
        PropertyValueStore store = new PropertyValueStore(iface, propertyValues);
        return simpleProxy(iface, store.createMethodInterpreter(), EqualisableByState.class, DynamicJsonBackedObject.class);
    }

}
