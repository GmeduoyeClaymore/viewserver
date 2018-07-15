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
import io.viewserver.datasource.Cardinality;
import io.viewserver.datasource.ContentType;
import io.viewserver.datasource.Dimension;
import io.viewserver.datasource.IDimensionMapper;
import io.viewserver.schema.column.*;

/**
 * Created by paulrg on 15/01/2015.
 */
public class UnEnumColumnNullableBool implements IColumnNullableBool {
    private final ContentType contentType;
    private final Cardinality cardinality;
    private ColumnHolder sourceHolder;
    private ColumnHolder outHolder;
    private String dataSource;
    private String dimension;
    private IDimensionMapper dimensionMapper;

    public UnEnumColumnNullableBool(ColumnHolder sourceHolder, ColumnHolder outHolder, String dataSource, IDimensionMapper dimensionMapper) {
        this.sourceHolder = sourceHolder;
        this.outHolder = outHolder;
        this.dataSource = dataSource;
        this.dimension = sourceHolder.getName();
        this.contentType = dimensionMapper.getContentType(dataSource,this.dimension);
        this.cardinality = dimensionMapper.getCardinality(dataSource,this.dimension);
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
    public NullableBool getNullableBool(int row) {
        int id = getLookupId(row);

        if (id == -1) {
            return NullableBool.Null;
        }

        return dimensionMapper.lookupNullableBool(dataSource, dimension, id);
    }

    @Override
    public boolean getBool(int row) {
        return getNullableBool(row) == NullableBool.True;
    }

    @Override
    public NullableBool getPreviousNullableBool(int row) {
        int id = getPreviousLookupId(row);
        if (id == -1) {
            return NullableBool.Null;
        }
        return dimensionMapper.lookupNullableBool(dataSource, dimension, id);
    }

    private int getPreviousLookupId(int row) {
        int id = -1;
        ColumnMetadata metadata = sourceHolder.getMetadata();
        switch (UnEnumColumnNullableBool.this.cardinality) {
            case Boolean: {
                if (sourceHolder instanceof IColumnNullableBool) {
                    id = ((IColumnNullableBool)sourceHolder).getPreviousNullableBool(row).getNumericValue();
                } else {
                    id = ((IColumnBool) sourceHolder).getPreviousBool(row) ? 1 : 0;
                }
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
                id = ((IColumnInt)sourceHolder).getPreviousInt(row);
                if (metadata != null) {
                    if (id == ((ColumnMetadataInt)metadata).getNullValue()) {
                        id = -1;
                    }
                }
                break;
            }
        }
        return id;
    }

    private int getLookupId(int row) {
        int id = -1;
        ColumnMetadata metadata = sourceHolder.getMetadata();
        switch (UnEnumColumnNullableBool.this.cardinality) {
            case Boolean: {
                if (sourceHolder instanceof IColumnNullableBool) {
                    id = ((IColumnNullableBool)sourceHolder).getNullableBool(row).getNumericValue();
                } else {
                    id = ((IColumnBool) sourceHolder).getBool(row) ? 1 : 0;
                }
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
                id = ((IColumnInt)sourceHolder).getInt(row);
                if (metadata != null) {
                    if (id == ((ColumnMetadataInt)metadata).getNullValue()) {
                        id = -1;
                    }
                }
                break;
            }
        }
        return id;
    }
}
