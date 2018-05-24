package io.viewserver.adapters.mongo;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class DynamicJsonBackedObjectCodec implements Codec<DynamicJsonBackedObject> {
    @Override
    public void encode(final BsonWriter writer, final DynamicJsonBackedObject value, final EncoderContext encoderContext) {
        writer.writeString(value.serialize());
    }

    @Override
    public DynamicJsonBackedObject decode(final BsonReader reader, final DecoderContext decoderContext) {
        return JSONBackedObjectFactory.create(reader.readString(),DynamicJsonBackedObject.class);
    }

    @Override
    public Class<DynamicJsonBackedObject> getEncoderClass() {
        return DynamicJsonBackedObject.class;
    }
}
