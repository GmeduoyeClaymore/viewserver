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
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TPrimitiveHash;

import java.util.Arrays;

//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * An open addressed hashing implementation for int primitives.
 *
 * Created: Sun Nov  4 08:56:06 2001
 *
 * @author Eric D. Friedman, Rob Eden, Jeff Randall
 * @version $Id: _E_Hash.template,v 1.1.2.6 2009/11/07 03:36:44 robeden Exp $
 */
abstract public class _KeyName_Hash extends TPrimitiveHash {
    static final long serialVersionUID = 1L;

    /** the set of longs */
    public transient int[] _set;
    protected transient _KeyType_[] _compactSet;
    protected int _lastUsedSlot = -1;
    private FifoBufferInt _freeSlots = new FifoBufferInt(8);
    private boolean _freeSlotsSorted = true;

    /**
     * value that represents null
     *
     * NOTE: should not be modified after the Hash is created, but is
     *       not final because of Externalization
     *
     */
    protected _KeyType_ no_entry_value;

    protected boolean consumeFreeSlot;


    /**
     * Creates a new <code>TLongHash</code> instance with the default
     * capacity and load factor.
     */
    public _KeyName_Hash() {
        super();
        setAutoCompactionFactor(0);
        Arrays.fill( _set, -1 );
        no_entry_value = Constants.Default_KeyName_NoEntryValue;
        //noinspection RedundantCast
        if ( no_entry_value != Constants._KeyName_NoopValue ) {
            Arrays.fill( _compactSet, no_entry_value );
        }
    }


    /**
     * Creates a new <code>TLongHash</code> instance whose capacity
     * is the next highest prime above <tt>initialCapacity + 1</tt>
     * unless that value is already prime.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public _KeyName_Hash(int initialCapacity) {
        super( initialCapacity );
        setAutoCompactionFactor(0);
        Arrays.fill( _set, -1 );
        no_entry_value = Constants.Default_KeyName_NoEntryValue;
        //noinspection RedundantCast
        if ( no_entry_value != Constants._KeyName_NoopValue ) {
            Arrays.fill( _compactSet, no_entry_value );
        }
    }


    /**
     * Creates a new <code>TLongHash</code> instance with a prime
     * value at or near the specified capacity and load factor.
     *
     * @param initialCapacity used to find a prime capacity for the table.
     * @param loadFactor used to calculate the threshold over which
     * rehashing takes place.
     */
    public _KeyName_Hash(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        setAutoCompactionFactor(0);
        Arrays.fill( _set, -1 );
        no_entry_value = Constants.Default_KeyName_NoEntryValue;
        //noinspection RedundantCast
        if ( no_entry_value != Constants._KeyName_NoopValue ) {
            Arrays.fill( _compactSet, no_entry_value );
        }
    }


    /**
     * Creates a new <code>TLongHash</code> instance with a prime
     * value at or near the specified capacity and load factor.
     *
     * @param initialCapacity used to find a prime capacity for the table.
     * @param loadFactor used to calculate the threshold over which
     * rehashing takes place.
     * @param no_entry_value value that represents null
     */
    public _KeyName_Hash(int initialCapacity, float loadFactor, _KeyType_ no_entry_value) {
        super(initialCapacity, loadFactor);
        setAutoCompactionFactor(0);
        Arrays.fill( _set, -1 );
        this.no_entry_value = no_entry_value;
        //noinspection RedundantCast
        if ( no_entry_value != Constants._KeyName_NoopValue ) {
            Arrays.fill( _compactSet, no_entry_value );
        }
    }


    /**
     * Returns the value that is used to represent null. The default
     * value is generally zero, but can be changed during construction
     * of the collection.
     *
     * @return the value that represents null
     */
    public _KeyType_ getNoEntryValue() {
        return no_entry_value;
    }


    /**
     * initializes the hashtable to a prime capacity which is at least
     * <tt>initialCapacity + 1</tt>.
     *
     * @param initialCapacity an <code>int</code> value
     * @return the actual capacity chosen
     */
    protected int setUp( int initialCapacity ) {
        int capacity;

        capacity = super.setUp( initialCapacity );
        _set = new int[capacity];
        _compactSet = new _KeyType_[capacity];
        return capacity;
    }


    /**
     * Searches the set for <tt>val</tt>
     *
     * @param val an <code>long</code> value
     * @return a <code>boolean</code> value
     */
    public boolean contains( _KeyType_ val ) {
        return index(val) >= 0;
    }


    /**
     * Executes <tt>procedure</tt> for each element in the set.
     *
     * @param procedure a <code>TObjectProcedure</code> value
     * @return false if the loop over the set terminated because
     * the procedure returned false for some value.
     */
//    public boolean forEach( T_KeyName_Procedure procedure ) {
//        byte[] states = _states;
//        int[] set = _set;
//        for ( int i = set.length; i-- > 0; ) {
//            if ( states[i] == FULL && ! procedure.execute( _compactSet[set[i]] ) ) {
//                return false;
//            }
//        }
//        return true;
//    }


    /**
     * Releases the element currently stored at <tt>index</tt>.
     *
     * @param index an <code>int</code> value
     */
    protected void removeAt( int index ) {
        int compactIndex = _set[index];
        _freeSlots.add(compactIndex);
        _freeSlotsSorted = false;
        _compactSet[compactIndex] = no_entry_value;
        _set[index] = -1;

        super.removeAt( index );
    }


    /**
     * Locates the index of <tt>val</tt>.
     *
     * @param val an <code>long</code> value
     * @return the index of <tt>val</tt> or -1 if it isn't in the set.
     */
    protected int index( _KeyType_ val ) {
        int hash, probe, index, length;

        final byte[] states = _states;
        final int[] set = _set;
        final _KeyType_[] compactSet = _compactSet;
        length = states.length;
        hash = HashFunctions.hash( val ) & 0x7fffffff;
        index = hash % length;
        byte state = states[index];

        if (state == FREE)
            return -1;

        if (state == FULL && EqualsFunctions.equals(compactSet[set[index]], val))
            return index;

        return indexRehashed(val, index, hash, state);
    }

    int indexRehashed(_KeyType_ key, int index, int hash, byte state) {
        // see Knuth, p. 529
        int length = _set.length;
        int probe = 1 + (hash % (length - 2));
        final int loopIndex = index;

        do {
            index -= probe;
            if (index < 0) {
                index += length;
            }
            state = _states[index];
            //
            if (state == FREE)
                return -1;

            //
            if (state != REMOVED && EqualsFunctions.equals(key, _compactSet[_set[index]]))
                return index;
        } while (index != loopIndex);

        return -1;
    }

    /**
     * Locates the index at which <tt>val</tt> can be inserted.  if
     * there is already a value equal()ing <tt>val</tt> in the set,
     * returns that value as a negative integer.
     *
     * @param val an <code>long</code> value
     * @return an <code>int</code> value
     */
    protected int insertKey( _KeyType_ val, int compactIndex ) {
        int hash, index;

        hash = HashFunctions.hash(val) & 0x7fffffff;
        index = hash % _states.length;
        byte state = _states[index];

        consumeFreeSlot = false;

        if (state == FREE) {
            consumeFreeSlot = true;
            insertKeyAt(index, val, compactIndex);

            return index;       // empty, all done
        }

        if (state == FULL && EqualsFunctions.equals(_compactSet[_set[index]], val)) {
            return -index - 1;   // already stored
        }

        // already FULL or REMOVED, must probe
        return insertKeyRehash(val, index, hash, state, compactIndex);
    }

    int insertKeyRehash(_KeyType_ val, int index, int hash, byte state, int compactIndex) {
        // compute the double hash
        final int length = _set.length;
        int probe = 1 + (hash % (length - 2));
        final int loopIndex = index;
        int firstRemoved = -1;

        /**
         * Look until FREE slot or we start to loop
         */
        do {
            // Identify first removed slot
            if (state == REMOVED && firstRemoved == -1)
                firstRemoved = index;

            index -= probe;
            if (index < 0) {
                index += length;
            }
            state = _states[index];

            // A FREE slot stops the search
            if (state == FREE) {
                if (firstRemoved != -1) {
                    insertKeyAt(firstRemoved, val, compactIndex);
                    return firstRemoved;
                } else {
                    consumeFreeSlot = true;
                    insertKeyAt(index, val, compactIndex);
                    return index;
                }
            }

            if (state == FULL && EqualsFunctions.equals(_compactSet[_set[index]], val)) {
                return -index - 1;
            }

            // Detect loop
        } while (index != loopIndex);

        // We inspected all reachable slots and did not find a FREE one
        // If we found a REMOVED slot we return the first one found
        if (firstRemoved != -1) {
            insertKeyAt(firstRemoved, val, compactIndex);
            return firstRemoved;
        }

        // Can a resizing strategy be found that resizes the set?
        throw new IllegalStateException("No free or removed slots available. Key set full?!!");
    }

    void insertKeyAt(int index, _KeyType_ val, int compactIndex) {
        if (compactIndex == -1) {
            compactIndex = getFreeSlot();
            _compactSet[compactIndex] = val;
        }
        _set[index] = compactIndex;  // insert value
        _states[index] = FULL;
    }

    private int getFreeSlot() {
        if (!_freeSlots.isEmpty()) {
//            if (!_freeSlotsSorted) {
//                _freeSlots.sort();
//                _freeSlotsSorted = true;
//            }
            return _freeSlots.remove();
        }
        return ++_lastUsedSlot;
    }

    @Override
    public void clear() {
        super.clear();
        _lastUsedSlot = -1;
        _freeSlots.clear();
        _freeSlotsSorted = true;
    }
}
