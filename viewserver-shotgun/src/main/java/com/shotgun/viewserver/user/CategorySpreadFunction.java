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
import java.util.stream.Collectors;

public class CategorySpreadFunction implements ISpreadFunction {
    private static final Logger log = LoggerFactory.getLogger(CategorySpreadFunction.class);
    private static final Column column = new Column("categoryIds",ContentType.String);

    @Override
    public List<Column> getColumns() {
        return Arrays.asList(column);
    }

    @Override
    public List<Map.Entry<Column, Object[]>> getValues(int row, ColumnHolder columnHolder) {
        List<Object> results = new ArrayList<>();
        String contentTypeJSONString = (String) ColumnHolderUtils.getValue(columnHolder, row);
        HashMap<String, Object> contentTypeConfiguration = ControllerUtils.mapDefault(contentTypeJSONString);
        for(Map.Entry<String, Object> str : contentTypeConfiguration.entrySet()){
            Object contentTypeConfig = str.getValue();
            if(contentTypeConfig != null){
                Map<String,Object> config = (Map<String, Object>) contentTypeConfig;
                Object productCategoriesForContentType = config.get("selectedProductCategories");
                if (productCategoriesForContentType != null) {
                    List<Map<String, Object>> productCategories = (List<Map<String, Object>>) productCategoriesForContentType;
                    if (productCategories != null) {
                        List<Map<String, Object>> finalProductCategories = productCategories;
                        List<Map<String, Object>> nonImplicitCategories = productCategories.stream().filter(cat -> this.isImplicit(cat, finalProductCategories)).collect(Collectors.toList());

                        for (Map<String, Object> entry : nonImplicitCategories) {
                            String path = getPath(entry);
                            results.add(path);
                        }
                    }
                }
            }
        }
        List<HashMap.Entry<Column,Object[]>> result = new ArrayList<>();
        result.add(new HashMap.SimpleEntry(column, result));
        return result;
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
