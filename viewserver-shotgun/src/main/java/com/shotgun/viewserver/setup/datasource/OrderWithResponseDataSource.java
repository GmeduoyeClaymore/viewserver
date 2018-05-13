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
                        new ProjectionNode("removeColsProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Exclusionary)
                                .withProjectionColumns(
                                    new IProjectionConfig.ProjectionColumn("dimension_partnerId")
                                ).withConnection(IDataSourceRegistry.getDefaultOperatorPath(OrderDataSource.NAME)),
                        new SpreadNode("orderResponseSpread")
                                .withInputColumn("orderDetails")
                                .withRemoveInputColumn()
                                .withSpreadFunction(DateNegotiatedOrderResponseSpreadFunction.NAME)
                                .withConnection("removeColsProjection", Constants.OUT, Constants.IN),
                        new JoinNode("responsePartnerJoin")
                                .withLeftJoinColumns(DateNegotiatedOrderResponseSpreadFunction.PARTNER_ID_COLUMN)
                                .withLeftJoinOuter()
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("", "responsePartner_")
                                .withAlwaysResolveNames()
                                .withConnection("orderResponseSpread", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(UserDataSource.NAME), Constants.OUT, "right"),
                        new ProjectionNode("orderSummaryProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn(DateNegotiatedOrderResponseSpreadFunction.ESTIMATED_DATE_COLUMN),
                                        new IProjectionConfig.ProjectionColumn(DateNegotiatedOrderResponseSpreadFunction.PRICE_COLUMN),
                                        new IProjectionConfig.ProjectionColumn(DateNegotiatedOrderResponseSpreadFunction.PARTNER_ID_COLUMN),
                                        new IProjectionConfig.ProjectionColumn(DateNegotiatedOrderResponseSpreadFunction.RESPONSE_STATUS),
                                        new IProjectionConfig.ProjectionColumn(DateNegotiatedOrderResponseSpreadFunction.ORDER_DETAIL_WITHOUT_RESPONSES, "orderDetails"),
                                        new IProjectionConfig.ProjectionColumn("customer_firstName"),
                                        new IProjectionConfig.ProjectionColumn("customer_lastName"),
                                        new IProjectionConfig.ProjectionColumn("customer_ratingAvg"),
                                        new IProjectionConfig.ProjectionColumn("customer_relationships"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_userId", "partner_userId"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_latitude", "partner_latitude"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_longitude", "partner_longitude"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_firstName", "partner_firstName"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_lastName", "partner_lastName"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_email", "partner_email"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_imageUrl", "partner_imageUrl"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_online", "partner_online"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_range", "partner_range"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_userStatus", "partner_userStatus"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_statusMessage", "partner_statusMessage"),
                                        new IProjectionConfig.ProjectionColumn("responsePartner_ratingAvg", "partner_ratingAvg"),
                                        new IProjectionConfig.ProjectionColumn("orderLocation"),
                                        new IProjectionConfig.ProjectionColumn("requiredDate"),
                                        new IProjectionConfig.ProjectionColumn("totalPrice"),
                                        new IProjectionConfig.ProjectionColumn("orderContentTypeId"),
                                        new IProjectionConfig.ProjectionColumn("orderId"),
                                        new IProjectionConfig.ProjectionColumn("userId"),
                                        new IProjectionConfig.ProjectionColumn("status"),
                                        new IProjectionConfig.ProjectionColumn("dimension_orderId"),
                                        new IProjectionConfig.ProjectionColumn("dimension_customerUserId"),
                                        new IProjectionConfig.ProjectionColumn("dimension_assignedPartnerUserId"),
                                        new IProjectionConfig.ProjectionColumn("dimension_status"),
                                        new IProjectionConfig.ProjectionColumn("dimension_contentTypeId"),
                                        new IProjectionConfig.ProjectionColumn("dimension_productId")
                                ).withConnection("responsePartnerJoin")
                )
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_orderId", "orderId", Cardinality.Byte, ContentType.String, true).withImported(),
                        new Dimension("dimension_customerUserId", "userId", Cardinality.Int, ContentType.String, true).withImported(),
                        new Dimension("dimension_partnerId", DateNegotiatedOrderResponseSpreadFunction.PARTNER_ID_COLUMN, Cardinality.Int, ContentType.String),
                        new Dimension("dimension_responseStatus", DateNegotiatedOrderResponseSpreadFunction.RESPONSE_STATUS, Cardinality.Int, ContentType.String),
                        new Dimension("dimension_status", "status", Cardinality.Int, ContentType.String, true).withImported(),
                        new Dimension("dimension_contentTypeId", "orderContentTypeId", Cardinality.Int, ContentType.Int, true).withImported(),
                        new Dimension("dimension_productId", Cardinality.Int, ContentType.String, true).withImported()))
                .withOutput("orderSummaryProjection")
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
