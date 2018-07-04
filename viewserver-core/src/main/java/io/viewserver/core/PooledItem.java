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

/**
 * Created by bemm on 26/06/15.
 */
public class PooledItem<T> {
    private T item;
    private IPool<T> owner;
    private int retainCount;

    public PooledItem(T item, IPool<T> owner) {
        this.item = item;
        this.owner = owner;
    }

    public T getItem() {
        return item;
    }

    public void retain() {
        retainCount++;
    }

    public void release() {
        if (retainCount <= 0) {
            throw new RuntimeException("Over-released pooled item!");
        }
        if (--retainCount == 0) {
            owner.release(this);
        }
    }
}
