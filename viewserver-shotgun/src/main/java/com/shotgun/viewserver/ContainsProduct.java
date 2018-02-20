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

package com.shotgun.viewserver;

import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.expression.function.Serialize;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionBool;
import io.viewserver.expression.tree.IExpressionDouble;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Paul on 19/11/2015.
 */
public class ContainsProduct implements IUserDefinedFunction, IExpressionBool {
    private static final Logger log = LoggerFactory.getLogger(Serialize.class);
    private IExpressionString contentTypeJSON;
    private IExpressionString categoryPath;
    private IExpressionString productId;

    @Override
    public void setParameters(IExpression... parameters) {
        if (parameters.length != 3) {
            throw new IllegalArgumentException("Syntax: containsProduct(<contentTypeJSON (string)>, <categoryPath (String)>, <productId (String)>, <unit (string) M|K|N>)");
        }

        contentTypeJSON = (IExpressionString) parameters[0];
        categoryPath = (IExpressionString) parameters[1];
        productId = (IExpressionString) parameters[2];
    }

    @Override
    public ColumnType getType() {
        return ColumnType.Bool;
    }

    @Override
    public boolean getBool(int row) {
        return containsProduct(contentTypeJSON.getString(row), categoryPath.getString(row), productId.getString(row));
    }

    private boolean containsProduct(String contentTypeJSONString, String productCategoryPath, String productId) {
        if(contentTypeJSONString == null || "".equals(contentTypeJSONString)){
            return false;
        }
        HashMap<String, Object> contentTypeConfiguration = ControllerUtils.mapDefault(contentTypeJSONString);
        for(Map.Entry<String, Object> str : contentTypeConfiguration.entrySet()){
            Object contentTypeConfig = str.getValue();
            if(contentTypeConfig != null){
                Map<String,Object> config = (Map<String, Object>) contentTypeConfig;
                if (matchOnProduct(productId, config)) return true;
                if(config.get("selectedProductIds")== null) {
                    if (matchOnProductCategory(productCategoryPath, config)) return true;
                }
            }
        }
        return false;
    }

    private boolean matchOnProductCategory(String productCategoryPath, Map<String, Object> config) {
        if(productCategoryPath != null) {
            Object productCategoriesForContentType = config.get("selectedProductCategories");
            if (productCategoriesForContentType != null) {
                List<Map<String, Object>> productCategories = (List<Map<String, Object>>) productCategoriesForContentType;
                if (productCategories != null) {
                    List<Map<String, Object>> finalProductCategories = productCategories;
                    List<Map<String, Object>> nonImplicitCategories = productCategories.stream().filter(cat -> this.isImplicit(cat, finalProductCategories)).collect(Collectors.toList());

                    for (Map<String, Object> entry : nonImplicitCategories) {
                        String path = getPath(entry);
                        if (path != null && isEqualOrWithinPath(path, productCategoryPath)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean matchOnProduct(String productId, Map<String, Object> config) {
        Object productIdsForContentType = config.get("selectedProductIds");
        if(productIdsForContentType != null){
            List<String> productIds = (List<String>) productIdsForContentType;
            if(productIds != null && productIds.contains(productId)){
                return true;
            }
        }
        return false;
    }

    private boolean isEqualOrWithinPath(String parent, String child) {
        return child.equals(parent) || isDescendent(parent,child);
    }

    boolean isImplicit(Map<String,Object> category, List<Map<String,Object>> categories){
        return categories.stream().filter(c-> isDecendentOf(c,category)).findAny().isPresent();
    }

    boolean isDecendentOf(Map<String,Object> parent,Map<String,Object> child){
        String parentPath = getPath(parent);
        String childPath = getPath(child);
        return isDescendent(parentPath, childPath);
    }

    private String getPath(Map<String, Object> child) {
        String childPath = (String) child.get("path");
        if(childPath == null){
            log.error("Found a category without a path is this duff data {}", ControllerUtils.toString(child));
        }
        return childPath;
    }

    private boolean isDescendent(String parentPath, String childPath) {
        if(childPath == null || parentPath == null)
            return false;
        return childPath.contains(parentPath + ">") && childPath.length() > parentPath.length();
    }


}