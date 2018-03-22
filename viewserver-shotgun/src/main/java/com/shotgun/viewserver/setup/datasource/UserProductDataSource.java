package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
UserProductDataSource {
    public static final String NAME = "userProduct";

    public static DataSource getDataSource(String firebaseKeyPath) {
        return new DataSource()
                .withName(NAME)
                .withNodes(
                        new ProjectionNode("userProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Projection)
                                .withProjectionColumns(Arrays.asList(
                                        new IProjectionConfig.ProjectionColumn("type", "userType"),
                                        new IProjectionConfig.ProjectionColumn("status", "userStatus"))
                                )
                                .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, UserDataSource.NAME)),
                        new JoinNode("productJoin")
                                .withLeftJoinColumns("userConstantJoinCol")
                                .withRightJoinColumns("prodConstantJoinCol")
                                .withColumnPrefixes("", "product_")
                                .withAlwaysResolveNames()
                                .withConnection("userProjection", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(ProductDataSource.NAME, ProductDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("productCategoryJoin")
                                .withLeftJoinColumns("product_categoryId")
                                .withRightJoinColumns("categoryId")
                                .withColumnPrefixes("", "productCategory_")
                                .withAlwaysResolveNames()
                                .withConnection("productJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(ProductCategoryDataSource.NAME, ProductCategoryDataSource.NAME), Constants.OUT, "right"),
                        new CalcColNode("userHasProductCalc")
                                .withCalculations(new CalcColOperator.CalculatedColumn("hasProduct", "containsProduct(selectedContentTypes, productCategory_path, product_productId)"))
                                .withConnection("productCategoryJoin"),
                        new FilterNode("hasProductFilter")
                                .withExpression("hasProduct")
                                .withConnection("userHasProductCalc")
                )
                .withOutput("hasProductFilter")
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
