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

import java.util.Arrays;

//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////

/**
 * An open addressed set implementation for long primitives.
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 */

public class _KeyName_HashSet extends _KeyName_Hash {
    static final long serialVersionUID = 1L;


    /**
     * Creates a new <code>TLongHashSet</code> instance with the default
     * capacity and load factor.
     */
    public _KeyName_HashSet() {
        super();
    }


    /**
     * Creates a new <code>TLongHashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initialCapacity</tt> and
     * with the default load factor.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public _KeyName_HashSet(int initialCapacity) {
        super( initialCapacity );
    }


    /**
     * Creates a new <code>TLongHash</code> instance with a prime
     * value at or near the specified capacity and load factor.
     *
     * @param initialCapacity used to find a prime capacity for the table.
     * @param load_factor used to calculate the threshold over which
     * rehashing takes place.
     */
    public _KeyName_HashSet(int initialCapacity, float load_factor) {
        super( initialCapacity, load_factor );
    }


    /**
     * Creates a new <code>TLongHashSet</code> instance with a prime
     * capacity equal to or greater than <tt>initial_capacity</tt> and
     * with the specified load factor.
     *
     * @param initial_capacity an <code>int</code> value
     * @param load_factor a <code>float</code> value
     * @param no_entry_value a <code>long</code> value that represents null.
     */
    public _KeyName_HashSet(int initial_capacity, float load_factor,
                            _KeyType_ no_entry_value) {
        super( initial_capacity, load_factor, no_entry_value );
        Arrays.fill(_set, -1);
        //noinspection RedundantCast
        if ( no_entry_value != Constants._KeyName_NoopValue ) {
            Arrays.fill( _compactSet, no_entry_value );
        }
    }


    /**
     * Creates a new <code>TLongHashSet</code> instance that is a copy
     * of the existing Collection.
     *
     * @param collection a <tt>Collection</tt> that will be duplicated.
     */
//    public _KeyName_HashSet(Collection<? extends Long> collection) {
//        this( Math.max( collection.size(), DEFAULT_CAPACITY ) );
//        addAll( collection );
//    }


    /**
     * Creates a new <code>TLongHashSet</code> instance that is a copy
     * of the existing set.
     *
     * @param collection a <tt>TLongSet</tt> that will be duplicated.
     */
//    public _KeyName_HashSet(T_KeyName_Collection collection) {
//        this( Math.max( collection.size(), DEFAULT_CAPACITY ) );
//        if ( collection instanceof _KeyName_HashSet) {
//            _KeyName_HashSet hashset = (_KeyName_HashSet) collection;
//            this._loadFactor = hashset._loadFactor;
//            Arrays.fill( _set, -1 );
//            this.no_entry_value = hashset.no_entry_value;
//            //noinspection RedundantCast
//            if ( this.no_entry_value != Constants._KeyName_NoopValue ) {
//                Arrays.fill( _compactSet, this.no_entry_value );
//            }
//            setUp( (int) Math.ceil( DEFAULT_CAPACITY / _loadFactor ) );
//        }
//        addAll( collection );
//    }


    /**
     * Creates a new <code>TLongHashSet</code> instance containing the
     * elements of <tt>array</tt>.
     *
     * @param array an array of <code>long</code> primitives
     */
//    public _KeyName_HashSet(_KeyType_[] array) {
//        this( Math.max( array.length, DEFAULT_CAPACITY ) );
//        addAll( array );
//    }


    /** {@inheritDoc} */
    public _KeyName_Iterator iterator() {
        return new _KeyName_HashIterator( this );
    }


    /** {@inheritDoc} */
    public _KeyType_[] toArray() {
        _KeyType_[] result = new _KeyType_[ size() ];
        int[] set = _set;
        byte[] states = _states;
        _KeyType_[] compactSet = _compactSet;

        for ( int i = states.length, j = 0; i-- > 0; ) {
            if ( states[i] == FULL ) {
                result[j++] = compactSet[set[i]];
            }
        }
        return result;
    }


    /** {@inheritDoc} */
    public _KeyType_[] toArray( _KeyType_[] dest ) {
        int[] set = _set;
        byte[] states = _states;
        _KeyType_[] compactSet = _compactSet;

        for ( int i = states.length, j = 0; i-- > 0; ) {
            if ( states[i] == FULL ) {
                dest[j++] = compactSet[set[i]];
            }
        }

        if ( dest.length > _size ) {
            dest[_size] = no_entry_value;
        }
        return dest;
    }


    /** {@inheritDoc} */
    public boolean add( _KeyType_ val ) {
        int index = insertKey(val, -1);

        if ( index < 0 ) {
            return false;       // already present in set, nothing to add
        }

        postInsertHook( consumeFreeSlot );

        return true;            // yes, we added something
    }

    public int add_KeyName_( _KeyType_ val ) {
        int index = insertKey(val, -1);

        if ( index < 0 ) {
            index = -index - 1;
            return -_set[index] - 1;
        }

        int compactIndex = _set[index];

        postInsertHook( consumeFreeSlot );

        return compactIndex;
    }

    @Override
    public int index(_KeyType_ val) {
        int index = super.index(val);
        if (index < 0) {
            return index;
        }
        return _set[index];
    }

    private int indexForRemove(_KeyType_ val) {
        return super.index(val);
    }

    public _KeyType_ get(int index) {
        return _compactSet[index];
    }

    /** {@inheritDoc} */
    public boolean remove( _KeyType_ val ) {
        int index = indexForRemove(val);
        if ( index >= 0 ) {
            removeAt( index );
            return true;
        }
        return false;
    }


    /** {@inheritDoc} */
//    public boolean containsAll( Collection<?> collection ) {
//        for ( Object element : collection ) {
//            if ( element instanceof Long ) {
//                long c = ( ( Long ) element ).longValue();
//                if ( ! contains( c ) ) {
//                    return false;
//                }
//            } else {
//                return false;
//            }
//
//        }
//        return true;
//    }


//    /** {@inheritDoc} */
//    public boolean containsAll( TLongCollection collection ) {
//        TLongIterator iter = collection.iterator();
//        while ( iter.hasNext() ) {
//            long element = iter.next();
//            if ( ! contains( element ) ) {
//                return false;
//            }
//        }
//        return true;
//    }


//    /** {@inheritDoc} */
//    public boolean containsAll( long[] array ) {
//        for ( int i = array.length; i-- > 0; ) {
//            if ( ! contains( array[i] ) ) {
//                return false;
//            }
//        }
//        return true;
//    }


    /** {@inheritDoc} */
//    public boolean addAll( Collection<? extends Long> collection ) {
//        boolean changed = false;
//        for ( Long element : collection ) {
//            long e = element.longValue();
//            if ( add( e ) ) {
//                changed = true;
//            }
//        }
//        return changed;
//    }


    /** {@inheritDoc} */
//    public boolean addAll( TLongCollection collection ) {
//        boolean changed = false;
//        TLongIterator iter = collection.iterator();
//        while ( iter.hasNext() ) {
//            long element = iter.next();
//            if ( add( element ) ) {
//                changed = true;
//            }
//        }
//        return changed;
//    }


    /** {@inheritDoc} */
//    public boolean addAll( long[] array ) {
//        boolean changed = false;
//        for ( int i = array.length; i-- > 0; ) {
//            if ( add( array[i] ) ) {
//                changed = true;
//            }
//        }
//        return changed;
//    }


    /** {@inheritDoc} */
//    @SuppressWarnings({"SuspiciousMethodCalls"})
//    public boolean retainAll( Collection<?> collection ) {
//        boolean modified = false;
//        TLongIterator iter = iterator();
//        while ( iter.hasNext() ) {
//            if ( ! collection.contains( Long.valueOf ( iter.next() ) ) ) {
//                iter.remove();
//                modified = true;
//            }
//        }
//        return modified;
//    }


//    /** {@inheritDoc} */
//    public boolean retainAll( TLongCollection collection ) {
//        if ( this == collection ) {
//            return false;
//        }
//        boolean modified = false;
//        TLongIterator iter = iterator();
//        while ( iter.hasNext() ) {
//            if ( ! collection.contains( iter.next() ) ) {
//                iter.remove();
//                modified = true;
//            }
//        }
//        return modified;
//    }


//    /** {@inheritDoc} */
//    public boolean retainAll( long[] array ) {
//        boolean changed = false;
//        Arrays.sort( array );
//        int[] set = _set;
//        byte[] states = _states;
//        long[] compactSet = _compactSet;
//
//        _autoCompactTemporaryDisable = true;
//        for ( int i = set.length; i-- > 0; ) {
//            if ( states[i] == FULL && ( Arrays.binarySearch( array, compactSet[set[i]] ) < 0) ) {
//                removeAt( i );
//                changed = true;
//            }
//        }
//        _autoCompactTemporaryDisable = false;
//
//        return changed;
//    }


    /** {@inheritDoc} */
//    public boolean removeAll( Collection<?> collection ) {
//        boolean changed = false;
//        for ( Object element : collection ) {
//            if ( element instanceof Long ) {
//                long c = ( ( Long ) element ).longValue();
//                if ( remove( c ) ) {
//                    changed = true;
//                }
//            }
//        }
//        return changed;
//    }


    /** {@inheritDoc} */
//    public boolean removeAll( TLongCollection collection ) {
//        boolean changed = false;
//        TLongIterator iter = collection.iterator();
//        while ( iter.hasNext() ) {
//            long element = iter.next();
//            if ( remove( element ) ) {
//                changed = true;
//            }
//        }
//        return changed;
//    }


    /** {@inheritDoc} */
//    public boolean removeAll( long[] array ) {
//        boolean changed = false;
//        for ( int i = array.length; i-- > 0; ) {
//            if ( remove(array[i]) ) {
//                changed = true;
//            }
//        }
//        return changed;
//    }


    /** {@inheritDoc} */
    public void clear() {
        super.clear();
        int[] set = _set;
        byte[] states = _states;
        _KeyType_[] compactSet = _compactSet;

        Arrays.fill(set, -1);
        Arrays.fill(states, FREE);
        Arrays.fill(compactSet, no_entry_value);
//        for ( int i = set.length; i-- > 0; ) {
//            set[i] = -1;
//            states[i] = FREE;
//            compactSet[i] = no_entry_value;
//        }
    }


    /** {@inheritDoc} */
    protected void rehash( int newCapacity ) {
        _KeyType_ oldCompactSet[] = _compactSet;

        _states = new byte[newCapacity];
        _set = new int[newCapacity];
        Arrays.fill(_set, -1);
        _compactSet = new _KeyType_[newCapacity];
        Arrays.fill(_compactSet, _lastUsedSlot + 1, newCapacity - 1, no_entry_value);

        for ( int i = 0; i <= _lastUsedSlot; i++) {
            _KeyType_ o = oldCompactSet[i];
            _compactSet[i] = o;
            if (o != no_entry_value) {
                insertKey(o, i);
            }
        }
    }


    /** {@inheritDoc} */
    public boolean equals( Object other ) {
        if ( ! ( other instanceof _KeyName_HashSet ) ) {
            return false;
        }
        _KeyName_HashSet that = ( _KeyName_HashSet ) other;
        if ( that.size() != this.size() ) {
            return false;
        }
        for ( int i = _states.length; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                if ( ! that.contains( _compactSet[_set[i]] ) ) {
                    return false;
                }
            }
        }
        return true;
    }


    /** {@inheritDoc} */
    public int hashCode() {
        int hashcode = 0;
        for ( int i = _states.length; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                hashcode += HashFunctions.hash( _compactSet[_set[i]] );
            }
        }
        return hashcode;
    }


    /** {@inheritDoc} */
    public String toString() {
        StringBuilder buffy = new StringBuilder( _size * 2 + 2 );
        buffy.append("{");
        for ( int i = _states.length, j = 1; i-- > 0; ) {
            if ( _states[i] == FULL ) {
                buffy.append( _compactSet[_set[i]] );
                if ( j++ < _size ) {
                    buffy.append( "," );
                }
            }
        }
        buffy.append("}");
        return buffy.toString();
    }


    class _KeyName_HashIterator extends HashPrimitiveIterator implements _KeyName_Iterator {

        /** the collection on which the iterator operates */
        private final _KeyName_Hash _hash;

        /** {@inheritDoc} */
        public _KeyName_HashIterator( _KeyName_Hash hash ) {
            super( hash );
            this._hash = hash;
        }

        /** {@inheritDoc} */
        public _KeyType_ next() {
            moveToNextIndex();
            _compactIndex = _set[_index];
            return _hash._compactSet[_compactIndex];
        }
    }


    /** {@inheritDoc} */
//    public void writeExternal( ObjectOutput out ) throws IOException {
//
//        // VERSION
//        out.writeByte( 1 );
//
//        // SUPER
//        super.writeExternal( out );
//
//        // NUMBER OF ENTRIES
//        out.writeInt( _size );
//
//        // LOAD FACTOR -- Added version 1
//        out.writeFloat( _loadFactor );
//
//        // NO ENTRY VALUE -- Added version 1
//        out.writeLong( no_entry_value );
//
//        // ENTRIES
//        for ( int i = _states.length; i-- > 0; ) {
//            if ( _states[i] == FULL ) {
//                out.writeLong( _compactSet[(int)_set[i]] );
//            }
//        }
//    }


    /** {@inheritDoc} */
//    public void readExternal( ObjectInput in )
//            throws IOException, ClassNotFoundException {
//
//        // VERSION
//        int version = in.readByte();
//
//        // SUPER
//        super.readExternal( in );
//
//        // NUMBER OF ENTRIES
//        int size = in.readInt();
//
//        if ( version >= 1 ) {
//            // LOAD FACTOR
//            _loadFactor = in.readFloat();
//
//            Arrays.fill( _set, -1 );
//            // NO ENTRY VALUE
//            no_entry_value = in.readLong();
//            //noinspection RedundantCast
//            if ( no_entry_value != ( long ) 0 ) {
//                Arrays.fill( _compactSet, no_entry_value );
//            }
//        }
//
//        // ENTRIES
//        setUp( size );
//        while ( size-- > 0 ) {
//            long val = in.readLong();
//            add( val );
//        }
//    }
} // TLongHashSet
