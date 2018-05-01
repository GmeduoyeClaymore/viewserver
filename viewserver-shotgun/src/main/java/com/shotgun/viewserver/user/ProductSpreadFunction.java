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

package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ContentType;
import io.viewserver.operators.spread.ISpreadFunction;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Created by Paul on 19/11/2015.
 */
public class ProductSpreadFunction implements ISpreadFunction {
    private static final Logger log = LoggerFactory.getLogger(ProductSpreadFunction.class);
    private static Column producId = new Column("spreadProductId", ContentType.String);


    @Override
    public List<Column> getColumns() {
        return Arrays.asList(producId);
    }

    @Override
    public List<Map.Entry<Column, Object[]>> getValues(int row, ColumnHolder columnHolder) {
        List<String> productIdsList = new ArrayList<>();
        String contentTypeJSONString = (String) ColumnHolderUtils.getValue(columnHolder, row);
        if(contentTypeJSONString ==null || "".equals(contentTypeJSONString)){
            return new ArrayList<>();
        }
        HashMap<String, Object> contentTypeConfiguration = ControllerUtils.mapDefault(contentTypeJSONString);
        for(Map.Entry<String, Object> str : contentTypeConfiguration.entrySet()){
            Object contentTypeConfig = str.getValue();
            if(contentTypeConfig != null){
                Map<String,Object> config = (Map<String, Object>) contentTypeConfig;
                Object productIdsForContentType = config.get("selectedProductIds");
                if(productIdsForContentType != null){
                    List<String> productIds = (List<String>) productIdsForContentType;
                    productIdsList.addAll(productIds);
                }
            }
        }
        List<HashMap.Entry<Column,Object[]>> result = new ArrayList<>();
        result.add(new HashMap.SimpleEntry(producId, productIdsList.toArray()));
        return result;
    }

}



