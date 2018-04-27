package io.viewserver.util.dynamic;

@FunctionalInterface
public interface UnboundMethodCallHandler<S> {
    MethodCallHandler bind(S state);
}
