package io.viewserver.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ControllerActionEntry{

    private final Class<?> parameterType;
    private final Class<?> returnType;
    private Method method;
    private Object controller;
    private static ObjectMapper mapper = new ObjectMapper();

    public ControllerActionEntry(Method method, Object controller, ControllerAction an, Controller controllerAttribute) {
        if(method.getParameterTypes().length > 1){
            throw new RuntimeException("Problem with constructing controller action \"" + an.path() + "\" on controller \"" + controllerAttribute.name() + "\". All actions must have a single parameter at most");
        }
        this.parameterType = method.getParameterTypes()[0];
        this.returnType = method.getReturnType();

        if(!mapper.canSerialize(this.parameterType)){
            throw new RuntimeException("Problem with constructing controller action \"" + an.path() + "\" on controller \"" + controllerAttribute.name() + "\". unable to serialize argument type \"" + this.parameterType + "\"");
        }

        if(!mapper.canSerialize(this.returnType)){
            throw new RuntimeException("Problem with constructing controller action \"" + an.path() + "\" on controller \"" + controllerAttribute.name() + "\". unable to serialize return type " + this.returnType);
        }
        this.method = method;
        this.controller = controller;
    }

    public String invoke(String param){
        Object arg = fromString(param, this.parameterType);
        try {
            Object result = method.invoke(this.controller, arg);
            return toString(result, this.returnType);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public static Object fromString(String ser,Class<?> aType){
        try {
            return mapper.readValue(ser, aType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to deserialize object \"" + ser + "\"",e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize object \"" + ser + "\"",e);
        }
    }

    public static String toString(Object ser,Class<?> aType){
        try {
            return mapper.writerFor(aType).writeValueAsString(ser);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize object \"" + ser + "\"",e);
        }
    }
}
