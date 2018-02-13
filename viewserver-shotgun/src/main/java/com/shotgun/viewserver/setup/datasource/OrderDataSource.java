package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.projection.IProjectionConfig;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
        OrderDataSource {
    public static final String NAME = "order";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/order.csv");
        Schema schema = new Schema()
                .withColumns(Arrays.asList(
                        new Column("orderId", "orderId", ColumnType.String),
                        new Column("created", "created", ColumnType.DateTime),
                        new Column("lastModified", "lastModified", ColumnType.DateTime),
                        new Column("status", "status", ColumnType.String),
                        new Column("userId", "userId", ColumnType.String),
                        new Column("paymentId", "paymentId", ColumnType.String),
                        new Column("deliveryId", "deliveryId", ColumnType.String),
                        new Column("totalPrice", "totalPrice", ColumnType.Double)/*,
                        new Column("rootProductCategory", "rootProductCategory", ColumnType.String)*/
                ))
                .withKeyColumns("orderId");

        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                dataAdapter,
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
                        new ProjectionNode("projectionNode")
                                .withMode(IProjectionConfig.ProjectionMode.Projection)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("contentType_contentTypeIdString", "contentTypeIdString")
                                ).withConnection("contentTypeJoin")
                      /*  new JoinNode("productJoin")
                                .withLeftJoinColumns("productId")
                                .withRightJoinColumns("productId")
                                .withColumnPrefixes("", "product_")
                                .withAlwaysResolveNames()
                                .withConnection("contentTypeJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(ProductDataSource.NAME, ProductDataSource.NAME), Constants.OUT, "right"),*/
                )

                .withOutput("projectionNode")
                .withDimensions(Arrays.asList(/*new Dimension("status", Cardinality.Int, ColumnType.String),*/ new Dimension("contentTypeIdString", Cardinality.Int, ColumnType.String, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
