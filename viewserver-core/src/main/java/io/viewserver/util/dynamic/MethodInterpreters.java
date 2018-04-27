package io.viewserver.util.dynamic;


import io.viewserver.controller.ControllerUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MethodInterpreters {

    private static final Method EQUALS_METHOD = Nonchalantly.invoke(() -> Object.class.getMethod("equals", Object.class));

    private MethodInterpreters() {
    }

    public static MethodInterpreter caching(MethodInterpreter interpreter) {
        return Memoizer.memoize(interpreter::interpret)::apply;
    }

    public static MethodInterpreter binding(Object target) {
        return binding(target, method -> {
            throw new IllegalStateException(String.format(
                    "Target class %s does not support method %s",
                    target.getClass(), method));
        });
    }

    public static MethodInterpreter binding(Object target, MethodInterpreter unboundInterpreter) {
        MethodCallHandler equaliser = getEqualiserFor(target);

        return method -> {
            if (method.equals(EQUALS_METHOD)) {
                return equaliser;
            }

            if (method.getDeclaringClass().isAssignableFrom(target.getClass())) {
                return (proxy, args) -> method.invoke(target, args);
            }

            return unboundInterpreter.interpret(method);
        };
    }

    private static MethodCallHandler getEqualiserFor(Object target) {
        if (target instanceof EqualisableByState) {
            Object targetState = ((EqualisableByState) target).getState();
            return (proxy, args) -> hasEqualState(targetState, args[0]);
        }

        return (proxy, args) -> target.equals(args[0]);
    }

    private static boolean hasEqualState(Object state, Object other) {
        return other instanceof EqualisableByState
                && state.equals(((EqualisableByState) other).getState());
    }

    public static MethodInterpreter intercepting(MethodInterpreter interpreter,
                                                 MethodCallInterceptor interceptor) {
        return method -> interceptor.intercepting(method, interpreter.interpret(method));
    }

    public static MethodInterpreter handlingDefaultMethods(MethodInterpreter nonDefaultInterpreter) {
        return method -> method.isDefault()
                ? DefaultMethodCallHandler.forMethod(method)

                : nonDefaultInterpreter.interpret(method);
    }

    public static MethodInterpreter handlingJsonMethods(Map<String, Object> propertyValues, MethodInterpreter nonDefaultInterpreter) {
        return method -> isExplicitJsonMethod(method)
                ? (pr,args) -> invokeExplicitJson(method,pr,args, propertyValues)
                : nonDefaultInterpreter.interpret(method);
    }

    private static Object invokeExplicitJson(Method method, Object pr, Object[] args, Map<String, Object> propertyValues) {
        if (method.getName().equals("serialize")) {
            List<String> propsToRemove = Arrays.asList((String[]) args[0]);
            HashMap<String, Object> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : propertyValues.entrySet()) {
                if (!propsToRemove.contains(entry.getKey())) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return ControllerUtils.toString(result);
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

    private static boolean isExplicitJsonMethod(Method method) {
        return (method.getName().equals("serialize") || method.getName().equals("set") || method.getName().equals("get"));
    }



}
