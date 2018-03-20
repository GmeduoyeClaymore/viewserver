package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
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

    public static DataSource getDataSource(String firebaseKeyPath) {
        Schema schema = new Schema()
                .withColumns(Arrays.asList(
                        new Column("orderId", "orderId", ColumnType.String),
                        new Column("created", "created", ColumnType.DateTime),
                        new Column("lastModified", "lastModified", ColumnType.DateTime),
                        new Column("status", "status", ColumnType.String),
                        new Column("userId", "userId", ColumnType.String),
                        new Column("paymentId", "paymentId", ColumnType.String),
                        new Column("deliveryId", "deliveryId", ColumnType.String),
                        new Column("totalPrice", "totalPrice", ColumnType.Double)
                ))
                .withKeyColumns("orderId");

        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                new FirebaseCsvDataAdapter(firebaseKeyPath, NAME, "data/order.csv"),
                                null
                        )
                )
                .withSchema(schema)
                .withNodes(
                        new JoinNode("deliveryJoin")
                                .withLeftJoinColumns("deliveryId")
                                .withRightJoinColumns("deliveryId")
                                .withConnection(OrderDataSource.NAME, Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(DeliveryDataSource.NAME, DeliveryDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("orderItemsJoin")
                                .withLeftJoinColumns("orderId")
                                .withRightJoinColumns("orderId")
                                .withConnection("deliveryJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(OrderItemsDataSource.NAME, OrderItemsDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("contentTypeJoin")
                                .withLeftJoinColumns("contentTypeId")
                                .withRightJoinColumns("contentTypeId")
                                .withAlwaysResolveNames()
                                .withColumnPrefixes("", "contentType_")
                                .withConnection("orderItemsJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(ContentTypeDataSource.NAME, ContentTypeDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("productJoin")
                                .withLeftJoinColumns("productId")
                                .withRightJoinColumns("productId")
                                .withColumnPrefixes("", "product_")
                                .withAlwaysResolveNames()
                                .withConnection("contentTypeJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(ProductDataSource.NAME, ProductDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("productCategoryJoin")
                                .withLeftJoinColumns("product_categoryId")
                                .withRightJoinColumns("categoryId")
                                .withColumnPrefixes("", "productCategory_")
                                .withAlwaysResolveNames()
                                .withConnection("productJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(ProductCategoryDataSource.NAME, ProductCategoryDataSource.NAME), Constants.OUT, "right"),
                        new ProjectionNode("projectionNode")
                                .withMode(IProjectionConfig.ProjectionMode.Projection)
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("contentType_dimension_contentTypeId", "dimension_contentTypeId"))
                                .withProjectionColumns(new IProjectionConfig.ProjectionColumn("product_dimension_productId", "dimension_productId"))
                                .withConnection("productCategoryJoin")
                )

                .withOutput("projectionNode")
                .withCalculatedColumns(
                        new CalculatedColumn("dimension_status", ColumnType.String, "status"),
                        new CalculatedColumn("dimension_orderId", ColumnType.String, "orderId"),
                        new CalculatedColumn("dimension_userId", ColumnType.String, "userId")
                )
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_orderId", Cardinality.Byte, ColumnType.String),
                        new Dimension("dimension_userId", Cardinality.Int, ColumnType.String),
                        new Dimension("dimension_driverId", Cardinality.Int, ColumnType.String, true),
                        new Dimension("dimension_productId", Cardinality.Int, ColumnType.String, true),
                        new Dimension("dimension_productCategoryId", Cardinality.Int, ColumnType.String, true),
                        new Dimension("dimension_status", Cardinality.Int, ColumnType.String),
                        new Dimension("dimension_contentTypeId", Cardinality.Int, ColumnType.Int, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
