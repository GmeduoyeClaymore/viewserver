// :_KeyName_=Bool,_KeyType_=boolean;_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double;_KeyName_=String,_KeyType_=String

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



package io.viewserver.collections;

import io.viewserver.core._KeyType_;
import gnu.trove.iterator.TIterator;

/**
 * Created by paulrg on 15/01/2015.
 */
public interface _KeyName_Iterator extends TIterator {
    /**
     * Advances the iterator to the next element in the underlying collection
     * and returns it.
     *
     * @return the next long in the collection
     * @exception NoSuchElementException if the iterator is already exhausted
     */
    public _KeyType_ next();
}
