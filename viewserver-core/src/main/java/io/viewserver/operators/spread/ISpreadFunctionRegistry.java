package io.viewserver.operators.spread;

public interface ISpreadFunctionRegistry {
    ISpreadFunction resolve(String name);
    void register(String name, Class<? extends ISpreadFunction>  function);
}
