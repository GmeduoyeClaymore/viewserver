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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Created by bemm on 03/12/15.
 */
public class ListWrapper<T> implements List<T> {
    private List<T> innerList;
    private final Consumer<T> addFunc;

    public ListWrapper(Consumer<T> addFunc) {
        this.addFunc = addFunc;
    }

    void setInnerList(List<T> innerList) {
        this.innerList = innerList;
    }

    @Override
    public int size() {
        return innerList != null ? innerList.size() : 0;
    }

    @Override
    public boolean isEmpty() {
        return innerList != null ? innerList.isEmpty() : true;
    }

    @Override
    public boolean contains(Object o) {
        return innerList != null ? innerList.contains(o) : false;
    }

    @Override
    public Iterator<T> iterator() {
        return innerList != null ? innerList.iterator() : null;
    }

    @Override
    public Object[] toArray() {
        return innerList != null ? innerList.toArray() : new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return innerList != null ? innerList.toArray(a) : a;
    }

    @Override
    public boolean add(T t) {
        add(size(), t);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("ListWrapper does not support removals - implement if required");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return innerList != null ? innerList.containsAll(c) : false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return addAll(size(), c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        for (T t : c) {
            add(index, t);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("ListWrapper does not support removals - implement if required");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort(Comparator<? super T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        if (innerList != null) {
            innerList.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        return innerList != null ? innerList.equals(o) : (o == null);
    }

    @Override
    public int hashCode() {
        return innerList != null ? innerList.hashCode() : 0;
    }

    @Override
    public T get(int index) {
        if (innerList != null) {
            return innerList.get(index);
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public T set(int index, T element) {
        if (innerList != null) {
            return innerList.set(index, element);
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void add(int index, T element) {
        addFunc.accept(element);
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("ListWrapper does not support removals - implement if required");
    }

    @Override
    public int indexOf(Object o) {
        return innerList != null ? innerList.indexOf(o) : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return innerList != null ? innerList.lastIndexOf(o) : -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<T> spliterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<T> stream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<T> parallelStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        if (innerList != null) {
            innerList.forEach(action);
        }
    }
}
