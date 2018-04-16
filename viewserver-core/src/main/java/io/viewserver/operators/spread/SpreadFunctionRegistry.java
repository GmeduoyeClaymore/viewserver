package io.viewserver.operators.spread;

import io.viewserver.expression.function.IUserDefinedFunction;

import java.util.HashMap;

public class SpreadFunctionRegistry implements ISpreadFunctionRegistry {

    HashMap<String, Class<? extends ISpreadFunction>> functionHashMap = new HashMap<>();

    public SpreadFunctionRegistry() {
        register("csv", CsvSpreadFunction.class);
    }

    @Override
    public ISpreadFunction resolve(String name){
        Class<? extends ISpreadFunction> clazz = functionHashMap.get(name);
        if (clazz == null) {
            return null;
        }

        try {
            return clazz.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(String name, Class<? extends ISpreadFunction>  function){
        this.functionHashMap.put(name, function);
    }
}
