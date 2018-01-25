package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gbemiga on 22/01/18.
 */
public class PricingStrategyResolver {

    private KeyedTable contentTypes;
    private KeyedTable productTable;
    private KeyedTable productCategoryTable;
    private HashMap<String,PriceStrategy> categoryToStrategyMap;
    private HashMap<String,PriceStrategy> categoryToStrategyCacheMap;

    private static final HashMap<String, PriceStrategy> NameToPricingStrategyMap;

    static{
        NameToPricingStrategyMap = new HashMap<String, PriceStrategy>();
        for (PriceStrategy strategy : EnumSet.allOf(PriceStrategy.class)) {
            NameToPricingStrategyMap.put(strategy.name(), strategy);
        }
    }

    public PricingStrategyResolver() {
        categoryToStrategyMap = new HashMap<>();
        categoryToStrategyCacheMap = new HashMap<>();
    }

    private HashMap<String,PriceStrategy> getCategoryToStrategyMap(){
        getContentTypeTable();
        return categoryToStrategyMap;
    }

    private KeyedTable getContentTypeTable(){
        if(this.contentTypes == null){
            this.contentTypes = ControllerUtils.getKeyedTable(TableNames.CONTENT_TYPE_TABLE_NAME);
            if(this.contentTypes != null){
                IOutput output = this.contentTypes.getOutput();
                IRowSequence rows = (output.getAllRows());

                while(rows.moveNext()){
                    String rootProductCategory = (String)ControllerUtils.getColumnValue(this.contentTypes, "rootProductCategory", rows.getRowId());
                    String pricingStrategy = (String)ControllerUtils.getColumnValue(this.contentTypes, "pricingStrategy", rows.getRowId());
                    categoryToStrategyMap.put(rootProductCategory, getValue(pricingStrategy));
                }
            }

        }
        return this.contentTypes;
    }

    private PriceStrategy getValue(String pricingStrategy) {
        return NameToPricingStrategyMap.get(pricingStrategy);
    }

    private KeyedTable getProductTable(){
        if(this.productTable == null){
            this.productTable = ControllerUtils.getKeyedTable(TableNames.PRODUCT_TABLE_NAME);
        }
        return this.productTable;
    }

    private KeyedTable getProductCategoryTable(){
        if(this.productCategoryTable == null){
            this.productCategoryTable = ControllerUtils.getKeyedTable(TableNames.PRODUCT_CATEGORY_TABLE_NAME);
        }
        return this.productCategoryTable;
    }

    public PriceStrategy resolve(String productId){
        String categoryId = getCategoryForProduct(productId);
        if(categoryToStrategyCacheMap.containsKey(categoryId)){
            return categoryToStrategyCacheMap.get(categoryId);
        }
        PriceStrategy strategyForCategoryRoot = getStrategyForCategoryRoot(categoryId);
        categoryToStrategyCacheMap.put(categoryId,strategyForCategoryRoot);
        return strategyForCategoryRoot;
   }

    private PriceStrategy getStrategyForCategoryRoot(String categoryId) {
        return getStrategyForCategoryRoot(categoryId,new ArrayList<>());
    }
    private PriceStrategy getStrategyForCategoryRoot(String categoryId,List<String> categoryPath) {
        if(categoryId == null){
            return null;
        }
        if(categoryPath.contains(categoryId)){
            throw new RuntimeException(String.format("Already found entry for category %s in path %s",categoryId,String.join(",",categoryPath)));
        }
        categoryPath.add(categoryId);
        if(this.getCategoryToStrategyMap().containsKey(categoryId)){
            return getCategoryToStrategyMap().get(categoryId);
        }
        int row = this.getProductCategoryTable().getRow(new TableKey(categoryId));
        if(row == -1){
            throw new RuntimeException(String.format("Cannot resolve pricing strategy for product category path \"%s\" Unable to find category id \"%s\" in the productCategory table",String.join(",",categoryPath),categoryId));
        }

        String parentCategoryId = (String) ControllerUtils.getColumnValue(this.getProductCategoryTable(), "parentCategoryId", row);
        return getStrategyForCategoryRoot(parentCategoryId,categoryPath);
    }

    private String getCategoryForProduct(String productId) {
        int row = this.getProductTable().getRow(new TableKey(productId));
        if(row == -1){
            throw new RuntimeException(String.format("Unable to find product id \"%s\" in the product table",productId));
        }
        return (String) ControllerUtils.getColumnValue(this.getProductTable(), "categoryId", row);
    }


}
