package io.viewserver.util.dynamic;

import java.lang.reflect.Method;

@FunctionalInterface
public interface UnboundMethodInterpreter<S> {

    UnboundMethodCallHandler<S> interpret(Method method);

    default MethodInterpreter bind(S state) {
        return method -> {
            UnboundMethodCallHandler<S> interpret = getInterpret(method);
            return interpret.bind(state);
        };
    }

    default UnboundMethodCallHandler<S> getInterpret(Method method) {
        return interpret(method);
    }
}
