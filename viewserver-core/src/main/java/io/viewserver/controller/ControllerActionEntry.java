package io.viewserver.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.*;
import io.viewserver.command.ActionParam;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;

public class ControllerActionEntry{

    private final Class<?> parameterType;
    private final List<ControllerParamEntry> actionParams;
    private boolean isFuture = false;
    private Method method;
    private Object controller;
    private static ObjectMapper mapper = new ObjectMapper();
    private ControllerAction an;
    private static TypeReference<HashMap<String,Object>> dictionaryType = new TypeReference<HashMap<String,Object>>() {};
    private static final Logger log = LoggerFactory.getLogger(ControllerActionEntry.class);

    public ControllerActionEntry(Method method, Object controller, ControllerAction an, Controller controllerAttribute) {
        this.an = an;
        this.actionParams =  getActionParams(method);
        this.parameterType = method.getParameterTypes().length == 1 ? method.getParameterTypes()[0] : null;

        if(this.parameterType != null && !mapper.canSerialize(this.parameterType)){
            throw new RuntimeException("Problem with constructing controller action \"" + an.path() + "\" on controller \"" + controllerAttribute.name() + "\". unable to serialize argument type \"" + this.parameterType + "\"");
        }

        this.method = method;
        this.controller = controller;
        if(this.method.getReturnType().isAssignableFrom(ListenableFuture.class)){
            this.isFuture = true;
        }
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
                    result.add(new ControllerParamEntry((ActionParam) an,parameterTypes[i],i));
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

    public String path() {
        return an.path();
    }


    public Class<?> returnType() {
        return this.method.getReturnType();
    }


    public String parameterJSON(){
        try{
            if(this.actionParams != null && actionParams.size() > 0){
                HashMap<String,Object> result = new HashMap<>();
                for(ControllerParamEntry str : this.actionParams){
                    result.put(str.name,getDefaultInstanceOf(str));
                }
                return mapper.writeValueAsString(result);
            }
            return this.parameterType == null ? null : mapper.writeValueAsString(getDefaultInstanceOf(this.parameterType));
        }catch (Exception ex){
            return ex.toString();
        }

    }

    private Object getDefaultInstanceOf(ControllerParamEntry controllerParamEntry) throws IllegalAccessException, JsonProcessingException, InstantiationException {
        String defaultValueForParam = controllerParamEntry.getAn() == null ? null : controllerParamEntry.getAn().exampleValue();
        return defaultValueForParam == null || "".equals(defaultValueForParam) ? getDefaultInstanceOf(controllerParamEntry.type) : (controllerParamEntry.type.isAssignableFrom(String.class) ? defaultValueForParam : mapper.writeValueAsString(defaultValueForParam));
    }

    private Object getDefaultInstanceOf(Class type) throws IllegalAccessException, InstantiationException, JsonProcessingException {
        if(type.isEnum()){
            return type.getEnumConstants()[0];
        }
        if(type.isAssignableFrom(String.class)){
            return "";
        }
        if(type.isAssignableFrom(int.class)){
            return 0;
        }
        if(type.isAssignableFrom(double.class)){
            return 0;
        }
        if(type.isAssignableFrom(Double.class)){
            return new Double(0);
        }
        if(type.isAssignableFrom(Date.class)){
            return mapper.writeValueAsString(Calendar.getInstance().getTime());
        }
        return  mapper.getTypeFactory().constructType(type);
}

    public ListenableFuture<String> invoke(String param, ControllerContext ctxt, ListeningExecutorService executorService){
        try {
            ListenableFuture result;
            if(this.actionParams != null && actionParams.size() > 0){
                HashMap<String,Object> map = mapper.readValue(param, dictionaryType);
                Object[] args = new Object[this.actionParams.size()];
                List<String> errors = new ArrayList<>();
                for(ControllerParamEntry paramEntry : this.actionParams){
                    Object parameter = map.get(paramEntry.getName());
                    if(parameter == null){
                        if(paramEntry.isRequired()){
                            errors.add(String.format("Unable to find parameter named \"%s\" in argument map \"%s\"",paramEntry.getName(),param));
                        }else{
                            args[paramEntry.index] = parameter;
                        }
                    }else{
                        try{
                            try{
                                if(paramEntry.getType().isInterface()){//can we deserialialize it
                                    args[paramEntry.index] = JSONBackedObjectFactory.create(getParameter(parameter), paramEntry.getType());
                                }
                                if(args[paramEntry.index] == null && toWrapperClass(paramEntry.getType()).isAssignableFrom(toWrapperClass(parameter.getClass()))){//can we just set it
                                    args[paramEntry.index] = parameter;
                                }
                                if(args[paramEntry.index] == null){////can type convert it
                                    args[paramEntry.index] = toObject(paramEntry.getType(),parameter);
                                }
                                if(args[paramEntry.index] == null){//can we deserialialize it
                                    args[paramEntry.index] = mapper.readValue(getParameter(parameter),paramEntry.getType());
                                }

                            }catch (Exception ex){
                                throw new RuntimeException(String.format("Problem deserializing parameter named \"%s\" ",paramEntry.getName())  + ex, ex);
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
                result = invokeMethod(executorService, ctxt,args);
            }
            else{
                Object arg = fromString(param, this.parameterType, this.an.path());
                result = this.parameterType == null ? invokeMethod(executorService, ctxt) : invokeMethod(executorService, ctxt,arg);
            }

            return result;
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

    private ListenableFuture<String> invokeMethod(ListeningExecutorService service,ControllerContext ctxt,Object... args) throws IllegalAccessException, InvocationTargetException {
        return this.invokeMethod(service,ctxt, () -> method.invoke(this.controller, args));
    }

    private ListenableFuture<String> invokeMethod(ListeningExecutorService service,ControllerContext ctxt) throws IllegalAccessException, InvocationTargetException {
        return this.invokeMethod(service,ctxt, () -> method.invoke(this.controller));
    }

    private ListenableFuture<String> invokeMethod(ListeningExecutorService service, ControllerContext ctxt, Callable<Object> method){
        if(isFuture){
            SettableFuture <String> result = SettableFuture.create();
            service.submit(() -> {
                ControllerContext.create(ctxt);
                ListenableFuture future = null;
                try {
                    future = (ListenableFuture)method.call();
                } catch (Exception e) {
                    log.error("Error with future", ControllerContext.Unwrap(e));
                    result.setException(ControllerContext.Unwrap(e));
                    return;
                }
                Futures.addCallback(future, new FutureCallback<Object>() {
                    @Override
                    public void onSuccess(Object o) {

                        result.set(ControllerActionEntry.toString(o));
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        result.setException(ControllerContext.Unwrap(throwable));
                    }
                });
            });
            return result;
        }
        return service.submit( () -> {
            try{
                ControllerContext.create(ctxt);
                return toString(method.call());
            }
            catch(Exception ex){
                throw new RuntimeException(ControllerContext.Unwrap(ex));
            }
        });
    }

    private String getParameter(Object parameter) {
        if(parameter == null){
            return null;
        }
        if(parameter instanceof String){
            return (String) parameter;
        }
        return toString(parameter);

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
        if(clazz.isEnum()){
            return Enum.valueOf(clazz,value.toString());
        }
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

    public static String toString(Object ser){
        if(ser == null){
            return null;
        }
        if( ser instanceof String){
            return (String) ser;
        }
        return ControllerUtils.toString(ser);

    }
}
