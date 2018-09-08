// :_KeyName_=Bool,_KeyType_=boolean;_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=String,_KeyType_=String

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

package io.viewserver.operators.unenum;

import io.viewserver.core.NullableBool;
import io.viewserver.core._KeyType_;
import io.viewserver.datasource.*;
import io.viewserver.schema.column.*;

/**
 * Created by paulrg on 15/01/2015.
 */
public class UnEnumColumn_KeyName_ implements IColumn_KeyName_, IWritableColumn, IUnEnumColumn {
    private final String dimensionName;
    private final Cardinality cardinality;
    private ColumnHolder sourceHolder;
    private ColumnHolder outHolder;
    private String dataSource;

    private ContentType contentType;
    private IDimensionMapper dimensionMapper;

    public UnEnumColumn_KeyName_(ColumnHolder sourceHolder, ColumnHolder outHolder, String dataSource, IDimensionMapper dimensionMapper) {
        this.sourceHolder = sourceHolder;
        this.outHolder = outHolder;
        this.dataSource = dataSource;
        this.dimensionName = sourceHolder.getName();
        this.contentType = dimensionMapper.getContentType(dataSource,this.dimensionName);
        this.cardinality = dimensionMapper.getCardinality(dataSource,this.dimensionName);
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    public String getName() {
        return sourceHolder.getName();
    }

    @Override
    public boolean supportsPreviousValues() {
        return true;
    }

    @Override
    public ColumnType getType() {
        return contentType.getColumnType();
    }

    @Override
    public _KeyType_ get_KeyName_(int row) {
        int id = getLookupId(row);

        if (id == -1) {
            ColumnMetadata metadata = outHolder.getMetadata();
            return metadata != null ?((ColumnMetadata_KeyName_)metadata).getNullValue() : (_KeyType_)ColumnType._KeyName_.getDefaultValue();
        }

        return dimensionMapper.lookup_KeyName_(dataSource, sourceHolder.getName(),id);
    }

    @Override
    public _KeyType_ getPrevious_KeyName_(int row) {
        int id = getPreviousLookupId(row);
        return dimensionMapper.lookup_KeyName_(dataSource, sourceHolder.getName(),id);
    }

    private int getPreviousLookupId(int row) {
        int id = getLookupId(row);
        ColumnMetadata metadata = sourceHolder.getMetadata();
        switch (this.cardinality) {
            case Boolean: {
                id = ((IColumnBool)sourceHolder).getPreviousBool(row) ? 1 : 0;
                break;
            }
            case Byte: {
                id = ((IColumnByte)sourceHolder).getPreviousByte(row);
                if (metadata != null) {
                    if (id == ((ColumnMetadataByte)metadata).getNullValue()) {
                        id = -1;
                    }
                }
                break;
            }
            case Short: {
                id = ((IColumnShort)sourceHolder).getPreviousShort(row);
                if (metadata != null) {
                    if (id == ((ColumnMetadataShort)metadata).getNullValue()) {
                        id = -1;
                    }
                }
                break;
            }
            case Int: {
                if(sourceHolder instanceof IColumnInt){
                    id = ((IColumnInt)sourceHolder).getPreviousInt(row);
                    if (metadata != null) {
                        if (id == ((ColumnMetadataInt)metadata).getNullValue()) {
                            id = -1;
                        }
                    }
                }else if(sourceHolder instanceof IColumnShort){
                    id = ((IColumnShort)sourceHolder).getPreviousShort(row);
                    if (metadata != null) {
                        if (id == ((ColumnMetadataShort)metadata).getNullValue()) {
                            id = -1;
                        }
                    }
                }
                else{
                    throw new RuntimeException(String.format("Could not unemum column %s as its type %s is neither int nor short",sourceHolder.getName(),sourceHolder.getClass()));
                }
                break;
            }
        }
        return id;
    }

    private int getLookupId(int row) {
        int id = -1;
        ColumnMetadata metadata = sourceHolder.getMetadata();
        switch (this.cardinality) {
            case Boolean: {
                id = NullableBool.fromBoolean(((IColumnBool) sourceHolder).getBool(row)).getNumericValue();
                break;
            }
            case Byte: {
                id = ((IColumnByte)sourceHolder).getByte(row);
                if (metadata != null) {
                    if (id == ((ColumnMetadataByte)metadata).getNullValue()) {
                        id = -1;
                    }
                }
                break;
            }
            case Short: {
                id = ((IColumnShort)sourceHolder).getShort(row);
                if (metadata != null) {
                    if (id == ((ColumnMetadataShort)metadata).getNullValue()) {
                        id = -1;
                    }
                }
                break;
            }
            case Int: {
                if(sourceHolder instanceof IColumnInt){
                    id = ((IColumnInt)sourceHolder).getInt(row);
                    if (metadata != null) {
                        if (id == ((ColumnMetadataInt)metadata).getNullValue()) {
                            id = -1;
                        }
                    }
                }else if(sourceHolder instanceof IColumnShort){
                    id = ((IColumnShort)sourceHolder).getShort(row);
                    if (metadata != null) {
                        if (id == ((ColumnMetadataShort)metadata).getNullValue()) {
                            id = -1;
                        }
                    }
                }
                else{
                    throw new RuntimeException(String.format("Could not unemum column %s as its type %s is neither int nor short",sourceHolder.getName(),sourceHolder.getClass()));
                }
                break;
            }
        }
        return id;
    }

    @Override
    public void storePreviousValues() {
        ((IWritableColumn)sourceHolder.getColumn()).storePreviousValues();
    }

    @Override
    public void resetAll() {
        // nothing to do?
    }
}
