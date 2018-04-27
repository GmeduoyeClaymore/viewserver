/*
package io.viewserver.util.dynamic;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.viewserver.controller.ControllerUtils;
import io.viewserver.core.JacksonSerialiser;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.viewserver.util.dynamic.MethodInterpreters.caching;

public class JSONPropertyBagInvocationHandler implements MethodInterpreter {

    private final Map<String, Object> target;
    Pattern methodTypePattern = Pattern.compile("<L([^;]+);>");

    public JSONPropertyBagInvocationHandler(Map<String, Object> target) {
        this.target = target;
    }

    private Class getListElementType(Method method) {
        try {
            java.lang.reflect.Field f = java.lang.reflect.Method.class.getDeclaredField("signature");
            f.setAccessible(true);
            String sigature = (String) f.get(method);
            Matcher matcher = methodTypePattern.matcher(sigature);
            if (matcher.find()) {
                String classString = matcher.group(1);
                return Class.forName(classString);
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public MethodCallHandler interpret(Method method) {
        return null;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodCallHandler handler) throws Throwable {

        if (method.isDefault())
        {
            final Class<?> declaringClass = method.getDeclaringClass();
            final MethodHandles.Lookup lookup = MethodHandles.publicLookup()
                    .in(declaringClass);

            // ensure allowed mode will not check visibility
            final Field f = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
            final int modifiers = f.getModifiers();
            if (Modifier.isFinal(modifiers)) { // should be done a single time
                final Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, modifiers & ~Modifier.FINAL);
                f.setAccessible(true);
                f.set(lookup, MethodHandles.Lookup.PRIVATE);
            }

            return lookup
                    .unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        }

        if (method.getName().equals("toString")) {
            return target.toString();
        }

        if (method.getName().equals("serialize")) {
            List<String> propsToRemove = Arrays.asList((String[]) args[0]);
            HashMap<String, Object> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : target.entrySet()) {
                if (!propsToRemove.contains(entry.getKey())) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return ControllerUtils.toString(result);
        }
        if (method.getName().equals("set")) {
            target.put((String) args[0], args[1]);
            return proxy;
        }
        if (method.getName().equals("get")) {
            return target.get(args[0]);
        }
        MethodInfo methodInfo = MethodInfo.forMethod(method);
        String propertyName = methodInfo.getPropertyName();

        if (methodInfo.isSetter()) {
            target.put(propertyName, args[0]);
            return null;
        }

        String json = ControllerUtils.toString(target.get(propertyName));
        if (json == null) {
            if (json == null && method.getReturnType().isAssignableFrom(List.class)) {
                List result = new ArrayList();
                target.put(propertyName, result);
                return result;
            }
            return null;
        }

        if (method.getReturnType().isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) method.getReturnType(), json);
        }

        if (method.getReturnType().isAssignableFrom(String.class)) {
            return json;
        }
        Object value;

        if (method.getReturnType().isAssignableFrom(List.class)) {
            Class listElementType = getListElementType(method);
            if (listElementType != null) {
                value = JacksonSerialiser.getInstance().deserialiseCollection(json, List.class, listElementType);
            } else {
                value = JacksonSerialiser.getInstance().deserialise(json, method.getReturnType());
            }
        } else {
            value = JacksonSerialiser.getInstance().deserialise(json, method.getReturnType());
        }

        if (value == null && method.getReturnType().isAssignableFrom(List.class)) {
            List result = new ArrayList();
            target.put(propertyName, result);
            return result;
        }

        return value;
    }


}
*/
