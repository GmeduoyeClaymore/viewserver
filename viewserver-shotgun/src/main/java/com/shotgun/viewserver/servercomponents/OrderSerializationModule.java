package com.shotgun.viewserver.servercomponents;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.shotgun.viewserver.delivery.ProductKey;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Date;

public class OrderSerializationModule extends SimpleModule {
    public OrderSerializationModule() {
        addSerializer(new ProductKeySerialiser());
        addSerializer(new DateSerialiser());
        addDeserializer(ProductKey.class, new ProductKeyDesSerialiser());
    }

    public static class ProductKeySerialiser extends StdSerializer<ProductKey> {
        public ProductKeySerialiser() {
            super(ProductKey.class);
        }

        @Override
        public void serialize(ProductKey value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(value.toString());
        }
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

    public static class ProductKeyDesSerialiser extends StdDeserializer<ProductKey> {
        public ProductKeyDesSerialiser() {
            super(ProductKey.class);
        }

        @Override
        public ProductKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final ObjectMapper mapper = (ObjectMapper) p.getCodec();
            final String node = mapper.readValue(p, String.class);
            return new ProductKey(node);
        }
    }
}
