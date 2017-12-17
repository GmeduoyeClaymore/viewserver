package io.viewserver.command;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ControllerActionEntry{

    private final Class<?> parameterType;
    private final Class<?> returnType;
    private final List<ControllerParamEntry> actionParams;
    private Method method;
    private Object controller;
    private static ObjectMapper mapper = new ObjectMapper();
    private ControllerAction an;
    private static TypeReference<HashMap<String,Object>> dictionaryType = new TypeReference<HashMap<String,Object>>() {};

    public ControllerActionEntry(Method method, Object controller, ControllerAction an, Controller controllerAttribute) {
        this.an = an;
        this.actionParams =  getActionParams(method);
        this.parameterType = method.getParameterTypes().length == 1 ? method.getParameterTypes()[0] : null;
        this.returnType = method.getReturnType();

        if(this.parameterType != null && !mapper.canSerialize(this.parameterType)){
            throw new RuntimeException("Problem with constructing controller action \"" + an.path() + "\" on controller \"" + controllerAttribute.name() + "\". unable to serialize argument type \"" + this.parameterType + "\"");
        }

        if(this.returnType != null && !mapper.canSerialize(this.returnType)){
            throw new RuntimeException("Problem with constructing controller action \"" + an.path() + "\" on controller \"" + controllerAttribute.name() + "\". unable to serialize return type " + this.returnType);
        }
        this.method = method;
        this.controller = controller;
    }

    private List<ControllerParamEntry> getActionParams(Method method) {
        List<ControllerParamEntry> result  = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for(int i=0;i<method.getParameterTypes().length;i++){
            Annotation[] annotations = method.getParameterAnnotations()[i];
            boolean found = false;
            for (Annotation an : annotations){
                if(an instanceof ActionParam){
                    found = true;
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    result.add(new ControllerParamEntry(((ActionParam) an).name(),parameterTypes[i],i));
                    continue;
                }
            }
            if(!found){
                errors.add(String.format("Parameter index %s does not contain an action parameter",i));
            }
        }
        if(errors.size() > 0){
            if(method.getParameterTypes().length > 1){
                throw new RuntimeException("Some parameters didn't have an action param attribute. This will only fly if you have single parameter methods\n" + String.join(",",errors));
            }
            return null;
        }
        return result;
    }

    public boolean isSynchronous(){
        return an.isSynchronous();
    }

    public String invoke(String param){
        Object arg = fromString(param, this.parameterType, this.an.path());
        try {
            Object result;
            if(this.actionParams != null && actionParams.size() > 0){
                HashMap<String,Object> map = mapper.readValue(param, dictionaryType);
                Object[] args = new Object[this.actionParams.size()];
                List<String> errors = new ArrayList<>();
                for(ControllerParamEntry paramEntry : this.actionParams){
                    Object parameter = map.get(paramEntry.getName());
                    if(parameter == null){
                        errors.add(String.format("Unable to find parameter named \"%s\" in argument map \"%s\"",paramEntry.getName(),param));
                    }else{
                        try{
                            try{
                                if(toWrapperClass(paramEntry.getType()).isAssignableFrom(toWrapperClass(parameter.getClass()))){//can we just set it
                                    args[paramEntry.index] = parameter;
                                }

                                if(args[paramEntry.index] == null){////can type convert it
                                    args[paramEntry.index] = toObject(paramEntry.getType(),parameter);
                                }

                                if(args[paramEntry.index] == null){//can we deserialialize it
                                    args[paramEntry.index] = mapper.readValue((String)parameter,paramEntry.getType());
                                }
                            }catch (Exception ex){
                                throw new RuntimeException(String.format("Problem deserializing parameter named \"%s\"",paramEntry.getName()));
                            }

                        }
                        catch (Exception ex){
                            errors.add(ex.getMessage());
                        }
                    }
                }
                if(errors.size() > 0){
                    throw new RuntimeException(String.format("Problems invoking method params \"%s\"", String.join(",",errors)));
                }
                result = method.invoke(this.controller, args);
            }
            else{
                result = this.parameterType == null ? method.invoke(this.controller) : method.invoke(this.controller, arg);
            }


            if(this.returnType == null){
                return null;
            }
            return toString(result, this.returnType);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> toWrapperClass(Class<?> type) {
        if (!type.isPrimitive())
            return type;
        else if (int.class.equals(type))
            return Integer.class;
        else if (double.class.equals(type))
            return Double.class;
        else if (char.class.equals(type))
            return Character.class;
        else if (boolean.class.equals(type))
            return Boolean.class;
        else if (long.class.equals(type))
            return Long.class;
        else if (float.class.equals(type))
            return Float.class;
        else if (short.class.equals(type))
            return Short.class;
        else if (byte.class.equals(type))
            return Byte.class;
        else
            throw new IllegalArgumentException("Primitive type not supported: " + type.getName());
    }

    public static Object toObject( Class clazz, Object value ) {
        if( Boolean.class == clazz  || boolean.class == clazz) return Boolean.parseBoolean( value + "" );
        if( Byte.class == clazz || byte.class == clazz) return Byte.parseByte( value  + "");
        if( Short.class == clazz || short.class == clazz) return Short.parseShort( value  + "");
        if( Integer.class == clazz || int.class == clazz ) return Integer.parseInt( value  + "");
        if( Long.class == clazz || long.class == clazz) return Long.parseLong( value  + "");
        if( Float.class == clazz || float.class == clazz) return Float.parseFloat( value  + "");
        if( Double.class == clazz || double.class == clazz ) return Double.parseDouble( value  + "");
        return null;
    }

    public static Object fromString(String ser,Class<?> aType, String action){
        if(aType == null){
            return null;
        }
        try {
            return mapper.readValue(ser, aType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to deserialize object \"" + ser + "\" in action " + action,e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize object \"" + ser + "\" in action " + action,e);
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
