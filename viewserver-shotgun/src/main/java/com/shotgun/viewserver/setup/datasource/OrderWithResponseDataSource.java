package com.shotgun.viewserver.setup.datasource;

import com.shotgun.viewserver.user.DateNegotiatedOrderResponseSpreadFunction;
import io.viewserver.Constants;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.execution.nodes.SpreadNode;
import io.viewserver.operators.projection.IProjectionConfig;

import java.util.Arrays;

public class OrderWithResponseDataSource {
    public static final String NAME = "orderWithResponses";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withNodes(
                        new SpreadNode("orderResponseSpread")
                                .withInputColumn("orderDetails")
                                .withRemoveInputColumn()
                                .withSpreadFunction(DateNegotiatedOrderResponseSpreadFunction.NAME)
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(OrderDataSource.NAME), Constants.OUT, Constants.IN),
                        new JoinNode("partnerJoin")
                                .withLeftJoinColumns(DateNegotiatedOrderResponseSpreadFunction.PARTNER_ID_COLUMN)
                                .withLeftJoinOuter()
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("", "partner_")
                                .withAlwaysResolveNames()
                                .withConnection("orderResponseSpread", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(UserDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("ratingJoin")
                                .withLeftJoinColumns("orderId", DateNegotiatedOrderResponseSpreadFunction.PARTNER_ID_COLUMN)
                                .withLeftJoinOuter()
                                .withRightJoinColumns("orderId", "userId")
                                .withConnection("partnerJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(RatingDataSource.NAME), Constants.OUT, "right"),
                        new ProjectionNode("orderSummaryProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn(DateNegotiatedOrderResponseSpreadFunction.ESTIMATED_DATE_COLUMN),
                                        new IProjectionConfig.ProjectionColumn(DateNegotiatedOrderResponseSpreadFunction.PRICE_COLUMN),
                                        new IProjectionConfig.ProjectionColumn(DateNegotiatedOrderResponseSpreadFunction.PARTNER_ID_COLUMN),
                                        new IProjectionConfig.ProjectionColumn(DateNegotiatedOrderResponseSpreadFunction.PARTNER_ORDER_STATUS),
                                        new IProjectionConfig.ProjectionColumn(DateNegotiatedOrderResponseSpreadFunction.ORDER_DETAIL_WITHOUT_RESPONSES, "orderDetails"),
                                        new IProjectionConfig.ProjectionColumn("customer_firstName"),
                                        new IProjectionConfig.ProjectionColumn("customer_lastName"),
                                        new IProjectionConfig.ProjectionColumn("customer_ratingAvg"),
                                        new IProjectionConfig.ProjectionColumn("partner_userId"),
                                        new IProjectionConfig.ProjectionColumn("partner_latitude"),
                                        new IProjectionConfig.ProjectionColumn("partner_latitude"),
                                        new IProjectionConfig.ProjectionColumn("partner_longitude"),
                                        new IProjectionConfig.ProjectionColumn("partner_firstName"),
                                        new IProjectionConfig.ProjectionColumn("partner_lastName"),
                                        new IProjectionConfig.ProjectionColumn("partner_email"),
                                        new IProjectionConfig.ProjectionColumn("partner_imageUrl"),
                                        new IProjectionConfig.ProjectionColumn("partner_online"),
                                        new IProjectionConfig.ProjectionColumn("partner_range"),
                                        new IProjectionConfig.ProjectionColumn("partner_userStatus"),
                                        new IProjectionConfig.ProjectionColumn("partner_statusMessage"),
                                        new IProjectionConfig.ProjectionColumn("partner_ratingAvg"),
                                        new IProjectionConfig.ProjectionColumn("orderLocation"),
                                        new IProjectionConfig.ProjectionColumn("requiredDate"),
                                        new IProjectionConfig.ProjectionColumn("totalPrice"),
                                        new IProjectionConfig.ProjectionColumn("orderContentTypeId"),
                                        new IProjectionConfig.ProjectionColumn("orderId"),
                                        new IProjectionConfig.ProjectionColumn("status"),
                                        new IProjectionConfig.ProjectionColumn("dimension_orderId"),
                                        new IProjectionConfig.ProjectionColumn("dimension_customerUserId"),
                                        new IProjectionConfig.ProjectionColumn("dimension_assignedPartnerUserId"),
                                        new IProjectionConfig.ProjectionColumn("dimension_status"),
                                        new IProjectionConfig.ProjectionColumn("dimension_contentTypeId"),
                                        new IProjectionConfig.ProjectionColumn("dimension_productId")
                                ).withConnection("ratingJoin")
                )
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_orderId", "orderId", Cardinality.Byte, ContentType.String, true).withImported(),
                        new Dimension("dimension_customerUserId", "userId", Cardinality.Int, ContentType.String, true).withImported(),
                        new Dimension("dimension_partnerId", DateNegotiatedOrderResponseSpreadFunction.PARTNER_ID_COLUMN, Cardinality.Int, ContentType.String),
                        new Dimension("dimension_partnerOrderStatus", DateNegotiatedOrderResponseSpreadFunction.PARTNER_ORDER_STATUS, Cardinality.Int, ContentType.String),
                        new Dimension("dimension_status", "status", Cardinality.Int, ContentType.String, true).withImported(),
                        new Dimension("dimension_contentTypeId", "orderContentTypeId", Cardinality.Int, ContentType.Int, true).withImported(),
                        new Dimension("dimension_productId", Cardinality.Int, ContentType.String, true).withImported()))
                .withOutput("orderSummaryProjection")
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
