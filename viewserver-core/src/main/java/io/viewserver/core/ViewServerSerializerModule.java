package io.viewserver.core;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.DynamicSerializationModule;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Date;

public class ViewServerSerializerModule extends DynamicSerializationModule {
    public ViewServerSerializerModule() {
        addDeserializer(Date.class, new DateDesSerialiser());
        addSerializer(new DateSerialiser());
        registerDynamicClass(DynamicJsonBackedObject.class);
    }

    public <T> void  registerDynamicClass(Class<T> dynamicClass){
        addSerializer(new DynamicJsonBackedObjectSerialiser(dynamicClass));
        addDeserializer(dynamicClass, new DynamicJsonBackedObjectDesSerialiser(dynamicClass));
    }


    public static class DateSerialiser extends StdSerializer<Date> {
        public DateSerialiser() {
            super(Date.class);
        }

        @Override
        public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(new DateTime(value).toString());
        }
    }

    public static class DynamicJsonBackedObjectDesSerialiser<TDynamicObject extends DynamicJsonBackedObject> extends StdDeserializer<TDynamicObject> {
        private Class<TDynamicObject> dynamicObjectClass;

        public DynamicJsonBackedObjectDesSerialiser(Class<TDynamicObject> dynamicObjectClass) {
            super(dynamicObjectClass);
            this.dynamicObjectClass = dynamicObjectClass;
        }

        @Override
        public TDynamicObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final ObjectMapper mapper = (ObjectMapper) p.getCodec();
            JsonNode node = mapper.readTree(p);
            String orderDetailsString = node.toString();
            return JSONBackedObjectFactory.create(orderDetailsString,dynamicObjectClass);
        }
    }

    public static class DynamicJsonBackedObjectSerialiser<TDynamicObject extends DynamicJsonBackedObject> extends StdSerializer<TDynamicObject> {
        public DynamicJsonBackedObjectSerialiser(Class<TDynamicObject> dynamicObjectClass) {
            super(dynamicObjectClass);
        }

        @Override
        public void serialize(TDynamicObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeObject(value.getFields());
        }
    }
}
