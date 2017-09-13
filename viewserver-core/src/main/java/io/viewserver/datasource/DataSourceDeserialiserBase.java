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

package io.viewserver.datasource;

import io.viewserver.util.ViewServerException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.List;

/**
 * Created by nick on 23/10/15.
 */
public abstract class DataSourceDeserialiserBase<T extends IDataSource> extends StdDeserializer<T> implements ResolvableDeserializer {
    private JsonDeserializer<?> defaultDeserialiser;

    public DataSourceDeserialiserBase(Class<?> clazz, JsonDeserializer<?> defaultDeserialiser) {
        super(clazz);
        this.defaultDeserialiser = defaultDeserialiser;
    }

    protected T doDeserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        T dataSource = (T) defaultDeserialiser.deserialize(p, ctxt);

        updateDimensionColumnReferences(dataSource);

        return dataSource;
    }

    private void updateDimensionColumnReferences(IDataSource dataSource) {
        List<Dimension> dimensions = dataSource.getDimensions();
        int count = dimensions.size();
        for (int i = 0; i < count; i++) {
            Dimension dimension = dimensions.get(i);
            Column column = dataSource.getSchema().getColumn(dimension.getName());
            if (column == null) {
                column = dataSource.getCalculatedColumn(dimension.getName());
                if (column == null) {
                    throw new ViewServerException("No column in data source for dimension '" + dimension.getName() + "'");
                }
            }
            dimension.setColumn(column);
        }
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        ((ResolvableDeserializer)defaultDeserialiser).resolve(ctxt);
    }}
