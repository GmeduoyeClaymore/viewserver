package io.viewserver.adapters.mongo;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class EnumCodec implements Codec<Enum> {


    @Override
    public Enum decode(BsonReader reader, DecoderContext decoderContext) {
        throw new RuntimeException("decode not implemented");
    }

    @Override
    public void encode(BsonWriter writer, Enum value, EncoderContext encoderContext) {
        writer.writeString(value.name());
    }

    @Override
    public Class<Enum> getEncoderClass() {
        return Enum.class;
    }
}
