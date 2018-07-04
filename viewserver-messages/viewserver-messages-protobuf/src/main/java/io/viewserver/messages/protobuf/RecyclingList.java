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

package io.viewserver.messages.protobuf;

import io.viewserver.messages.IRecyclableMessage;
import io.viewserver.messages.MessagePool;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Created by bemm on 03/12/15.
 */
public abstract class RecyclingList<TMessage extends IRecyclableMessage, TDto> implements List<TMessage> {
    private Class<TMessage> messageClass;
    protected List<TDto> dtoList;
    private TMessage message;

    public RecyclingList(Class<TMessage> messageClass) {
        this.messageClass = messageClass;
    }

    void setDtoList(List<TDto> dtoList) {
        this.dtoList = dtoList;
    }

    protected abstract void doAdd(Object dto);

    public List<TDto> getDtoList() {
        return dtoList;
    }

    @Override
    public int size() {
        return dtoList != null ? dtoList.size() : 0;
    }

    @Override
    public boolean isEmpty() {
        return dtoList != null ? dtoList.isEmpty() : true;
    }

    @Override
    public boolean contains(Object o) {
        return dtoList != null ? dtoList.contains(o) : false;
    }

    @Override
    public Iterator<TMessage> iterator() {
        return new Iterator<TMessage>() {
            Iterator<TDto> dtoListIterator = dtoList.iterator();
            @Override
            public boolean hasNext() {
                return dtoListIterator.hasNext();
            }
            @Override
            public TMessage next() {
                final TMessage message = getMessage();
                message.setDto(dtoListIterator.next());
                return (TMessage) message.retain();
            }
        };
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    public boolean add(TMessage message) {
        message.retain();
        doAdd(message.getDto());
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends TMessage> c) {
        c.forEach(this::add);
        return true;
    }

    public boolean addAll(int index, Collection<? extends TMessage> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public void replaceAll(UnaryOperator<TMessage> operator) {
        throw new UnsupportedOperationException();
    }

    public void sort(Comparator<? super TMessage> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        if (message != null) {
            message.release();
            message = null;
        }
        doClear();
    }

    protected abstract void doClear();

    @Override
    public boolean equals(Object o) {
        return dtoList != null ? dtoList.equals(o) : (o == null);
    }

    @Override
    public int hashCode() {
        return dtoList != null ? dtoList.hashCode() : 0;
    }

    @Override
    public TMessage get(int index) {
        if (dtoList == null) {
            throw new IllegalStateException("Cannot get from a null dto list!");
        }
        final TMessage message = getMessage();
        message.setDto(dtoList.get(index));
        return (TMessage) message.retain();
    }

    public TMessage set(int index, TMessage element) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, TMessage element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TMessage remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<TMessage> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<TMessage> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TMessage> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<TMessage> spliterator() {
        throw new UnsupportedOperationException();
    }

    public boolean removeIf(Predicate<? super TMessage> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<TMessage> stream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<TMessage> parallelStream() {
        throw new UnsupportedOperationException();
    }

    public void forEach(Consumer<? super TMessage> action) {
        throw new UnsupportedOperationException();
    }

    private TMessage getMessage() {
        if (message != null) {
            message.release();
        }
        message = MessagePool.getInstance().get(messageClass);
        return message;
    }

    public void release() {
        if (message != null) {
            message.release();
            message = null;
        }
        dtoList = null;
    }
}
