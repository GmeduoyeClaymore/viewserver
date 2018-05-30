package io.viewserver.adapters.mongo;

import io.viewserver.core.JacksonSerialiser;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.MapCodec;
import org.bson.codecs.configuration.CodecRegistry;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;

public class DynamicJsonBackedObjectArrayCodec implements Codec<DynamicJsonBackedObject[]> {

    private final Codec codec;

    public DynamicJsonBackedObjectArrayCodec(CodecRegistry codecRegistry) {
        codec = codecRegistry.get(DynamicJsonBackedObject.class);
    }


    @Override
    public void encode(final BsonWriter writer, final DynamicJsonBackedObject[] value, final EncoderContext encoderContext) {

        writer.writeStartArray();

        for (DynamicJsonBackedObject obj:value) {
            encoderContext.encodeWithChildContext(codec, writer, obj);
        }

        writer.writeEndArray();

    }

    @Override
    public DynamicJsonBackedObject[] decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new NotImplementedException();
    }

    @Override
    public Class<DynamicJsonBackedObject[]> getEncoderClass() {
        return DynamicJsonBackedObject[].class;
    }
}
