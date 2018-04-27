package io.viewserver.util.dynamic;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.stream.Stream;

import static io.viewserver.util.dynamic.MethodInterpreters.caching;
import static io.viewserver.util.dynamic.MethodInterpreters.handlingDefaultMethods;
import static io.viewserver.util.dynamic.MethodInterpreters.intercepting;

@FunctionalInterface
public interface MethodInterpreter extends InvocationHandler {

    @Override
    default Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodCallHandler handler = interpret(method);
        return handler.invoke(proxy, args);
    }

    MethodCallHandler interpret(Method method);
}


