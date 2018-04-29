package io.viewserver.util.dynamic;

import io.viewserver.controller.ControllerUtils;
import io.viewserver.core.JacksonSerialiser;
import org.apache.commons.beanutils.ConvertUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PropertyMappingClassInterpreter {

    private static final ClassInterpreter<Map<String, Object>> cached = ClassInterpreter.cached(
            ClassInterpreter.mappingWith(PropertyMappingClassInterpreter::interpret));

    static Pattern methodTypePattern = Pattern.compile("<L([^;]+);>");

    public static UnboundMethodInterpreter<Map<String, Object>> interpret(Class<?> iface) {
        return cached.interpret(iface);
    }

    private PropertyMappingClassInterpreter() {
    }

    private static UnboundMethodCallHandler<Map<String, Object>> interpret(Method method) {
        MethodInfo info = MethodInfo.forMethod(method);

        if(method.isDefault()){
            return (meth) -> DefaultMethodCallHandler.forMethod(method);
        }

        if(isExplicitJsonMethod(method)){
            return jsonMethodHandler(info);
        }

        if (info.isGetter()) {
            return getterHandler(info);
        }
        if (info.isSetter()) {
            return setterHandler(info.getPropertyName());
        }

        throw new IllegalArgumentException(String.format("Method %s is neither a getter nor a setter method", method));
    }

    private static UnboundMethodCallHandler<Map<String, Object>> jsonMethodHandler(MethodInfo info) {
        return propertyValues -> (proxy, args) -> invokeExplicitJson(info.getMethod(),proxy,args,propertyValues);
    }

    private static boolean isExplicitJsonMethod(Method method) {
        return (method.getName().equals("serialize") || method.getName().equals("set") || method.getName().equals("get"))  || method.getName().equals("getFields");
    }

    private static UnboundMethodCallHandler<Map<String, Object>> getterHandler(MethodInfo info) {
        return propertyValues -> (proxy, args) -> getValueWithTypingForGenericLists(info,propertyValues);
    }

    private static Object getValueWithTypingForGenericLists(MethodInfo info, Map<String, Object> target) {
        Method method = info.getMethod();
        Object value = target.get(info.getPropertyName());

        if(value == null){
            return null;
        }

        if (method.getReturnType().isAssignableFrom(value.getClass())) {
            return value;
        }
//Convert value to string then into required object to coerce into the correct shape then cache result
        String jsonRepresentation = JacksonSerialiser.getInstance().serialise(value);

        Object deserialise = JacksonSerialiser.getInstance().deserialise(jsonRepresentation, method.getReturnType());

        target.put(info.getPropertyName(), deserialise);

        return deserialise;
    }

    private static Object invokeExplicitJson(Method method, Object pr, Object[] args, Map<String, Object> propertyValues) {
        if (method.getName().equals("serialize")) {
            HashMap<String, Object> result = getFields(args[0], propertyValues);
            try
            {
                return ControllerUtils.toString(result);
            }
            catch (Exception ex){
                if(ex.getMessage().contains("Direct self-reference leading to cycle")){
                    throw new RuntimeException("Problem serializing object have all of the nested types been registered as dynamic types");
                }
                throw ex;
            }
        }
        if (method.getName().equals("getFields")) {
            return getFields(args[0], propertyValues);
        }
        if (method.getName().equals("set")) {
            propertyValues.put((String) args[0], args[1]);
            return null;
        }
        if (method.getName().equals("get")) {
            return propertyValues.get(args[0]);
        }
        throw new RuntimeException(method.getName() + " is not an explicit json method so we should not be in here");
    }

    private static HashMap<String, Object> getFields(Object arg, Map<String, Object> propertyValues) {
        List<String> propsToRemove = Arrays.asList((String[]) arg);
        HashMap<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : propertyValues.entrySet()) {
            if (!propsToRemove.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static UnboundMethodCallHandler<Map<String, Object>> setterHandler(String propertyName) {
        return propertyValues -> (proxy, args) -> {
            Object value = args[0];
            if (value == null) {
                propertyValues.remove(propertyName);
            } else {
                propertyValues.put(propertyName, value);
            }
            return null;
        };
    }


}
