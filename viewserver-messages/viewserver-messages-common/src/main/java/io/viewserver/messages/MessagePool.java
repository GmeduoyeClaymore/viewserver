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

package io.viewserver.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by nick on 02/12/15.
 */
public class MessagePool implements IMessagePool {
    private static final MessagePool instance = new MessagePool();
    private final Map<Class, Pool> pools = new HashMap<>();

    public <T extends IPoolableMessage> void createPool(Class<T> clazz, IFactory<T> factory) {
        pools.put(clazz, new Pool<>(this, factory));
    }

    public static MessagePool getInstance() {
        return instance;
    }

    @Override
    public <T extends IPoolableMessage> T get(Class<T> clazz) {
        final Pool<T> pool = pools.get(clazz);
        if (pool == null) {
            throw new IllegalArgumentException(String.format("No pool exists for class %s", clazz.getName()));
        }
        return pool.get();
    }

    @Override
    public <T extends IPoolableMessage> void release(Class<T> clazz, IPoolableMessage message) {
        final Pool pool = pools.get(clazz);
        if (pool == null) {
            throw new IllegalArgumentException(String.format("No pool exists for class %s", clazz.getName()));
        }
        pool.release(message);
    }

    private static class Pool<T extends IPoolableMessage> {
        private final ConcurrentLinkedDeque<T> items = new ConcurrentLinkedDeque<>();
        private MessagePool owner;
        private final IFactory<T> factory;

        private Pool(MessagePool owner, IFactory<T> factory) {
            this.owner = owner;
            this.factory = factory;
        }

        public T get() {
            T item = items.poll();
            if (item == null) {
                item = factory.create();
                item.setOwner(owner);
            }
            item.retain();
            return item;
        }

        public void release(T item) {
            items.add(item);
        }
    }

    public interface IFactory<T> {
        T create();
    }
}
