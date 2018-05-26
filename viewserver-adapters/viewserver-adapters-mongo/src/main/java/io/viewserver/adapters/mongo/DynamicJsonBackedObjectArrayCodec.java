package io.viewserver.adapters.mongo;

import io.viewserver.core.JacksonSerialiser;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DynamicJsonBackedObjectArrayCodec implements Codec<DynamicJsonBackedObject[]> {
    @Override
    public void encode(final BsonWriter writer, final DynamicJsonBackedObject[] value, final EncoderContext encoderContext) {
        writer.writeString(JacksonSerialiser.getInstance().serialise(value));
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
