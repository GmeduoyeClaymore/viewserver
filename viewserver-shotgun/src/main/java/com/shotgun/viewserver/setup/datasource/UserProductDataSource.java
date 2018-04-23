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
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(UserDataSource.NAME)),
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
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(ProductDataSource.NAME), Constants.OUT, "right"),
                        new ProjectionNode("projectionNode")
                                .withMode(IProjectionConfig.ProjectionMode.Projection)
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("product_dimension_productId", "dimension_productId"))
                                .withConnection("productJoin")
                )
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_productId", Cardinality.Int, ContentType.String, true).withImported()))
                .withOutput("projectionNode")
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
