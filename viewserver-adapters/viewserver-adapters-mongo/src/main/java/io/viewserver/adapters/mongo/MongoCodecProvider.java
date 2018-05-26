package io.viewserver.adapters.mongo;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class MongoCodecProvider implements CodecProvider {
        @Override
        public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
            if (clazz.getName().startsWith("com.sun.proxy.$") || DynamicJsonBackedObject.class.isAssignableFrom(clazz)) {
                return (Codec<T>) new DynamicJsonBackedObjectCodec();
            }
            if(clazz.isArray() && DynamicJsonBackedObject.class.isAssignableFrom(clazz.getComponentType())){
                return (Codec<T>) new DynamicJsonBackedObjectArrayCodec();
            }

            return null;
        }
}

