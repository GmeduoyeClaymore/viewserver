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

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by bemm on 26/06/15.
 */
public class Pool<T> implements IPool<T> {
    private final ConcurrentLinkedQueue<PooledItem<T>> availableItems;
    private IPoolItemFactory<T> factory;
    private int initialSize;
    private IPoolItemInitialiser<T> initialiser;

    public Pool(IPoolItemFactory<T> factory, int initialSize) {
        this.factory = factory;
        this.initialSize = initialSize;
        availableItems = new ConcurrentLinkedQueue<>();
    }

    public Pool(IPoolItemFactory<T> factory, int initialSize, IPoolItemInitialiser<T> initialiser) {
        this(factory, initialSize);
        this.initialiser = initialiser;
        fillPool();
    }

    @Override
    public PooledItem<T> take() {
        PooledItem<T> item = availableItems.poll();
        if (item == null) {
            item = createNewItem();
        }
        item.retain();
        return item;
    }

    private void fillPool() {
        for (int i = 0; i < initialSize; i++) {
            availableItems.add(createNewItem());
        }
    }

    private PooledItem<T> createNewItem() {
        PooledItem<T> item = new PooledItem<>(factory.create(), this);
        if (initialiser != null) {
            initialiser.initialise(item.getItem());
        }
        return item;
    }

    @Override
    public void release(PooledItem<T> item) {
        if (initialiser != null) {
            initialiser.initialise(item.getItem());
        }
        availableItems.add(item);
    }
}
