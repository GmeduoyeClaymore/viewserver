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

package io.viewserver.schema.column;

import io.viewserver.core._KeyType_;
import io.viewserver.schema.column.chunked.ChunkedColumn_KeyName_;

/**
 * Created by bemm on 26/09/2014.
 */
public class ColumnHolder_KeyName_ extends ColumnHolder implements IColumn_KeyName_ {
    public ColumnHolder_KeyName_(String name) {
        super(name);
        setMetadata(new ColumnMetadata_KeyName_());
    }

    public ColumnHolder_KeyName_(String name, IRowMapper rowMapper) {
        super(name, rowMapper);
        setMetadata(new ColumnMetadata_KeyName_());
    }

    public ColumnHolder_KeyName_(IColumn column, IRowMapper rowMapper) {
        super(column, rowMapper);
        setMetadata(new ColumnMetadata_KeyName_());
    }

    @Override
    public boolean supportsPreviousValues() {
        return ((IColumn_KeyName_)getColumn()).supportsPreviousValues();
    }

    @Override
    public ColumnType getType() {
        return ColumnType._KeyName_;
    }

    @Override
    public _KeyType_ get_KeyName_(int row) {
        int sourceRow = getSourceRow(row);
        if (sourceRow == -1) {
            return ChunkedColumn_KeyName_.DEFAULT_VALUE;
        }
        return ((IColumn_KeyName_)getColumn()).get_KeyName_(sourceRow);
    }

    @Override
    public _KeyType_ getPrevious_KeyName_(int row) {
        int sourceRow = getSourceRow(row);
        if (sourceRow == -1) {
            return ChunkedColumn_KeyName_.DEFAULT_VALUE;
        }
        return ((IColumn_KeyName_)getColumn()).getPrevious_KeyName_(sourceRow);
    }

    @Override
    public ColumnMetadata_KeyName_ getMetadata() {
        return (ColumnMetadata_KeyName_)super.getMetadata();
    }
}
