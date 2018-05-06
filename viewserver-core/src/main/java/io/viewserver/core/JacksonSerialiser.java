/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bemm on 09/12/2014.
 */
public class JacksonSerialiser implements IJsonSerialiser {
    private static final Logger log = LoggerFactory.getLogger(JacksonSerialiser.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<JavaType, ObjectReader> readers = new HashMap<>();
    private static JacksonSerialiser instance;




    public static JacksonSerialiser getInstance(){
        if(instance == null){
            instance = new JacksonSerialiser();
        }
        return instance;
    }

    public JacksonSerialiser() {
        registerModules(new Module[]{new AfterburnerModule(),
                new ParameterNamesModule(),
                new ValueListSerialisationModule()});
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void registerModules(Module[] modules) {
        mapper.registerModules(
                modules
        );
    }

    @Override
    public String serialise(Object object) {
        if(object != null && object.getClass().isAssignableFrom(DynamicJsonBackedObject.class)){
            return ((DynamicJsonBackedObject)object).serialize();
        }
        return serialise(object, false);
    }

    @Override
    public String serialise(Object object, boolean prettyPrint) {
        try {
            if(prettyPrint){
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
            }

            if(object == null){
                return null;
            }

            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            if(ex.getMessage().contains("Direct self-reference leading to cycle")){
                throw new RuntimeException("Problem serializing object have all of the nested types been registered as dynamic types");
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <T> T deserialise(String json, Class<T> type) {
        return internalDeserialise(json, mapper.getTypeFactory().constructType(type));
    }

    @Override
    public <T> T deserialiseCollection(String json, Class<? extends Collection> collectionClass, Class elementClass) {
        return internalDeserialise(json, mapper.getTypeFactory().constructCollectionType(collectionClass, elementClass));
    }

    private <T> T internalDeserialise(String json, JavaType type) {
        if(json == null || "".equals(json)){
            return null;
        }
        ObjectReader reader = readers.get(type);
        if (reader == null) {
            reader = mapper.reader(type);
            readers.put(type, reader);
        }
        try {
            if(type.getRawClass().isAssignableFrom(DynamicJsonBackedObject.class)){
                return JSONBackedObjectFactory.create(json, (Class<T>) type.getRawClass());
            }
            return reader.readValue(json);
        } catch (IOException e) {
            log.error("Failed to deserialise " + type + " from " + json, e);
            return null;
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    private class ParameterNamesModule extends SimpleModule {
        @Override
        public void setupModule(SetupContext context) {
            super.setupModule(context);
            context.insertAnnotationIntrospector(new ParameterNamesAnnotationIntrospector());
        }
    }

    private class ParameterNamesAnnotationIntrospector extends NopAnnotationIntrospector {
        @Override
        public boolean hasCreatorAnnotation(Annotated a) {
            // if there is no default constructor, and this constructor has more than one parameter (and therefore
            // is not, by definition, a delegating constructor), then implicitly mark it as creator
            if (a instanceof AnnotatedConstructor) {
                AnnotatedConstructor constructor = (AnnotatedConstructor) a;
                AnnotatedClass contextClass = constructor.getContextClass();
                if (constructor.getParameterCount() > 1 && contextClass.getDefaultConstructor() == null
                        && contextClass.getConstructors().size() == 1) {
                    return true;
                }
                if (constructor.getParameterCount() == 1) {
                    AnnotatedParameter parameter = constructor.getParameter(0);
                    if (parameter.getParameterType() instanceof Class &&
                            (parameter.getParameterType() == String.class || ((Class)parameter.getParameterType()).isPrimitive())) {
                        return true;
                    }
                }
            }
            return super.hasCreatorAnnotation(a);
        }

        @Override
        public String findImplicitPropertyName(AnnotatedMember m) {
            if (m instanceof AnnotatedParameter) {
                return findParameterName((AnnotatedParameter) m);
            }
            return null;
        }

        private String findParameterName(AnnotatedParameter annotatedParameter) {

            AnnotatedWithParams owner = annotatedParameter.getOwner();
            Parameter[] params;

            if (owner instanceof AnnotatedConstructor) {
                params = ((AnnotatedConstructor) owner).getAnnotated().getParameters();
            } else if (owner instanceof AnnotatedMethod) {
                params = ((AnnotatedMethod) owner).getAnnotated().getParameters();
            } else {
                return null;
            }
            Parameter p = params[annotatedParameter.getIndex()];
            return p.isNamePresent() ? p.getName() : null;
        }
    }
}
