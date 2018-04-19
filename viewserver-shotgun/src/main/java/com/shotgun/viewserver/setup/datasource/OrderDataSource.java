package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.projection.IProjectionConfig;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class OrderDataSource {
    public static final String NAME = "order";

    public static DataSource getDataSource() {
        SchemaConfig schema = new SchemaConfig()
                .withColumns(Arrays.asList(
                        new Column("orderId", ContentType.String),
                        new Column("created", ContentType.DateTime),
                        new Column("lastModified", ContentType.DateTime),
                        new Column("status", ContentType.String),
                        new Column("userId", ContentType.String),
                        new Column("paymentId", ContentType.String),
                        new Column("deliveryId", ContentType.String),
                        new Column("totalPrice", ContentType.Int)
                ))
                .withKeyColumns("orderId");

        return new DataSource()
                .withName(NAME)
                .withSchema(schema)
                .withNodes(
                        new JoinNode("deliveryJoin")
                                .withLeftJoinColumns("deliveryId")
                                .withRightJoinColumns("deliveryId")
                                .withConnection(OrderDataSource.NAME, Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(DeliveryDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("orderItemsJoin")
                                .withLeftJoinColumns("orderId")
                                .withRightJoinColumns("orderId")
                                .withConnection("deliveryJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(OrderItemsDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("contentTypeJoin")
                                .withLeftJoinColumns("contentTypeId")
                                .withRightJoinColumns("contentTypeId")
                                .withAlwaysResolveNames()
                                .withColumnPrefixes("", "contentType_")
                                .withConnection("orderItemsJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(ContentTypeDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("productJoin")
                                .withLeftJoinColumns("productId")
                                .withRightJoinColumns("productId")
                                .withColumnPrefixes("", "product_")
                                .withAlwaysResolveNames()
                                .withConnection("contentTypeJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(ProductDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("productCategoryJoin")
                                .withLeftJoinColumns("product_categoryId")
                                .withRightJoinColumns("categoryId")
                                .withColumnPrefixes("", "productCategory_")
                                .withAlwaysResolveNames()
                                .withConnection("productJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(ProductCategoryDataSource.NAME), Constants.OUT, "right"),
                        new ProjectionNode("projectionNode")
                                .withMode(IProjectionConfig.ProjectionMode.Projection)
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("contentType_dimension_contentTypeId", "dimension_contentTypeId"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("product_dimension_productId", "dimension_productId"))
                                .withConnection("productCategoryJoin")
                )

                .withOutput("projectionNode")
                .withCalculatedColumns(
                        new CalculatedColumn("dimension_status", ContentType.String, "status"),
                        new CalculatedColumn("dimension_orderId", ContentType.String, "orderId"),
                        new CalculatedColumn("dimension_customerUserId", ContentType.String, "userId")
                )
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_orderId", Cardinality.Byte, ContentType.String),
                        new Dimension("dimension_customerUserId", Cardinality.Int, ContentType.String, true),
                        new Dimension("dimension_driverId", Cardinality.Int, ContentType.String, true),
                        new Dimension("dimension_productId", Cardinality.Int, ContentType.String, true),
                        new Dimension("dimension_productCategoryId", Cardinality.Int, ContentType.String, true),
                        new Dimension("dimension_status", Cardinality.Int, ContentType.String),
                        new Dimension("dimension_contentTypeId", Cardinality.Int, ContentType.Int, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
