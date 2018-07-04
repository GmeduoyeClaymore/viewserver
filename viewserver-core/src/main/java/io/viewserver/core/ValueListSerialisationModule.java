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

import io.viewserver.messages.common.ValueLists;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by bemm on 15/12/15.
 */
public class ValueListSerialisationModule extends SimpleModule {
    public ValueListSerialisationModule() {
        addSerializer(new ValueListSerialiser());
        addDeserializer(ValueLists.IValueList.class, new ValueListDeserialiser());
    }

    public static class ValueListSerialiser extends StdSerializer<ValueLists.IValueList> {
        public ValueListSerialiser() {
            super(ValueLists.IValueList.class);
        }

        @Override
        public void serialize(ValueLists.IValueList value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            final Object[] array = value.toArray();
            jgen.writeStartArray();
            for (int i = 0; i < array.length; i++) {
                jgen.writeObject(array[i]);
            }
            jgen.writeEndArray();
        }
    }

    public static class ValueListDeserialiser extends StdDeserializer<ValueLists.IValueList> {
        public ValueListDeserialiser() {
            super(ValueLists.IValueList.class);
        }

        @Override
        public ValueLists.IValueList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final ObjectMapper mapper = (ObjectMapper) p.getCodec();
            final ArrayNode node = mapper.readTree(p);
            final int size = node.size();
            if (size == 0) {
                return ValueLists.EMPTY_LIST;
            }
            final Object[] values = new Object[size];
            for (int i = 0; i < node.size(); i++) {
                final JsonNode valueNode = node.get(i);
                if (valueNode.isTextual()) {
                    values[i] = valueNode.textValue();
                } else if (valueNode.isBoolean()) {
                    values[i] = valueNode.booleanValue();
                } else if (valueNode.isShort()) {
                    values[i] = valueNode.shortValue();
                } else if (valueNode.isInt()) {
                    values[i] = valueNode.intValue();
                } else if (valueNode.isLong()) {
                    values[i] = valueNode.longValue();
                } else if (valueNode.isFloat()) {
                    values[i] = valueNode.floatValue();
                } else if (valueNode.isDouble()) {
                    values[i] = valueNode.doubleValue();
                } else {
                    throw new UnsupportedOperationException(String.format("Unhandled value list value '%s' in json", node));
                }
            }
            return ValueLists.valueListOf(values);
        }
    }
}
