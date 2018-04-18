package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.execution.nodes.SpreadNode;
import io.viewserver.operators.projection.IProjectionConfig;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
UserProductDataSource {
    public static final String NAME = "userProduct";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withNodes(
                        new ProjectionNode("userProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Projection)
                                .withProjectionColumns(Arrays.asList(
                                        new IProjectionConfig.ProjectionColumn("type", "userType"),
                                        new IProjectionConfig.ProjectionColumn("userStatus", "userStatus"))
                                )
                                .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, UserDataSource.NAME)),
                        new SpreadNode("productsSpread")
                                .withInputColumn("selectedContentTypes")
                                .withOutputColumn("spreadProductId")
                                .withSpreadFunction("getProductIdsFromContentTypeJSON")
                                .withConnection("userProjection", Constants.OUT, Constants.IN),
                        new JoinNode("productJoin")
                                .withLeftJoinColumns("spreadProductId")
                                .withRightJoinColumns("productId")
                                .withColumnPrefixes("", "product_")
                                .withAlwaysResolveNames()
                                .withConnection("productsSpread", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(ProductDataSource.NAME, ProductDataSource.NAME), Constants.OUT, "right")
                )
                .withCalculatedColumns(
                        new CalculatedColumn("dimension_productId", ColumnType.String, "product_productId")
                )
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_productId", Cardinality.Int, ColumnType.String, true)))
                .withOutput("productJoin")
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
