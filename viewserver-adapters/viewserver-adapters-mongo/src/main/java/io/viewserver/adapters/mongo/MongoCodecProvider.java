package io.viewserver.adapters.mongo;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MongoCodecProvider implements CodecProvider {
        @Override
        public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
            if (clazz.getName().startsWith("com.sun.proxy.$")) {
                return (Codec<T>) new DynamicJsonBackedObjectCodec();
            }

            return null;
        }
}

