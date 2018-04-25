package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.DeliveryAddressDataSource;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import com.shotgun.viewserver.setup.datasource.RatingDataSource;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class DriverOrderSummaryReport {
        public static final String ID = "driverOrderSummary";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "driverOrderSummary")
                        .withDataSource(OrderDataSource.NAME)
                        .withNodes(
                                new JoinNode("customerJoin")
                                        .withLeftJoinColumns("userId")
                                        .withRightJoinColumns("userId")
                                        .withConnection("#input", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, "ratingJoin"), Constants.OUT, "right"),
                                new JoinNode("ratingJoin")
                                        .withLeftJoinColumns("orderId", "userId")
                                        .withLeftJoinOuter()
                                        .withRightJoinColumns("orderId", "userId")
                                        .withConnection("customerJoin", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(RatingDataSource.NAME, RatingDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("originDeliveryAddressJoin")
                                        .withLeftJoinColumns("originDeliveryAddressId")
                                        .withRightJoinColumns("deliveryAddressId")
                                        .withColumnPrefixes("", "origin_")
                                        .withAlwaysResolveNames()
                                        .withConnection("ratingJoin", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(DeliveryAddressDataSource.NAME, DeliveryAddressDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("destinationDeliveryAddressJoin")
                                        .withLeftJoinColumns("destinationDeliveryAddressId")
                                        .withLeftJoinOuter()
                                        .withRightJoinColumns("deliveryAddressId")
                                        .withColumnPrefixes("", "destination_")
                                        .withAlwaysResolveNames()
                                        .withConnection("originDeliveryAddressJoin", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(DeliveryAddressDataSource.NAME, DeliveryAddressDataSource.NAME), Constants.OUT, "right"),
                                new ProjectionNode("orderSummaryProjection")
                                        .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                        .withProjectionColumns(
                                                new IProjectionConfig.ProjectionColumn("orderId"),
                                                new IProjectionConfig.ProjectionColumn("productId"),
                                                new IProjectionConfig.ProjectionColumn("totalPrice"),
                                                new IProjectionConfig.ProjectionColumn("notes"),
                                                new IProjectionConfig.ProjectionColumn("imageUrl"),
                                                new IProjectionConfig.ProjectionColumn("quantity"),
                                                new IProjectionConfig.ProjectionColumn("paymentId"),
                                                new IProjectionConfig.ProjectionColumn("deliveryId"),
                                                new IProjectionConfig.ProjectionColumn("fixedPrice"),
                                                new IProjectionConfig.ProjectionColumn("startTime"),
                                                new IProjectionConfig.ProjectionColumn("endTime"),
                                                new IProjectionConfig.ProjectionColumn("userId", "customerUserId"),
                                                new IProjectionConfig.ProjectionColumn("rating", "customerRating"),
                                                new IProjectionConfig.ProjectionColumn("ratingAvg", "customerRatingAvg"),
                                                new IProjectionConfig.ProjectionColumn("firstName", "customerFirstName"),
                                                new IProjectionConfig.ProjectionColumn("lastName", "customerLastName"),
                                                new IProjectionConfig.ProjectionColumn("status"),
                                                new IProjectionConfig.ProjectionColumn("created"),
                                                new IProjectionConfig.ProjectionColumn("distance"),
                                                new IProjectionConfig.ProjectionColumn("duration"),
                                                new IProjectionConfig.ProjectionColumn("product_productId", "productProductId"),
                                                new IProjectionConfig.ProjectionColumn("product_name", "productName"),
                                                new IProjectionConfig.ProjectionColumn("product_imageUrl", "productImageUrl"),
                                                new IProjectionConfig.ProjectionColumn("contentType_contentTypeId", "contentTypeContentTypeId"),
                                                new IProjectionConfig.ProjectionColumn("contentType_name", "contentTypeName"),
                                                new IProjectionConfig.ProjectionColumn("contentType_hasOrigin", "contentTypeHasOrigin"),
                                                new IProjectionConfig.ProjectionColumn("contentType_hasDestination", "contentTypeHasDestination"),
                                                new IProjectionConfig.ProjectionColumn("contentType_hasStartTime", "contentTypeHasStartTime"),
                                                new IProjectionConfig.ProjectionColumn("contentType_hasEndTime", "contentTypeHasEndTime"),
                                                new IProjectionConfig.ProjectionColumn("contentType_rootProductCategory", "contentTypeRootProductCategory"),
                                                new IProjectionConfig.ProjectionColumn("contentType_pricingStrategy", "contentTypePricingStrategy"),
                                                new IProjectionConfig.ProjectionColumn("origin_flatNumber", "originFlatNumber"),
                                                new IProjectionConfig.ProjectionColumn("origin_line1", "originLine1"),
                                                new IProjectionConfig.ProjectionColumn("origin_city", "originCity"),
                                                new IProjectionConfig.ProjectionColumn("origin_postCode", "originPostCode"),
                                                new IProjectionConfig.ProjectionColumn("origin_latitude", "originLatitude"),
                                                new IProjectionConfig.ProjectionColumn("origin_longitude", "originLongitude"),
                                                new IProjectionConfig.ProjectionColumn("destination_flatNumber", "destinationFlatNumber"),
                                                new IProjectionConfig.ProjectionColumn("destination_line1", "destinationLine1"),
                                                new IProjectionConfig.ProjectionColumn("destination_city", "destinationCity"),
                                                new IProjectionConfig.ProjectionColumn("destination_postCode", "destinationPostCode"),
                                                new IProjectionConfig.ProjectionColumn("destination_latitude", "destinationLatitude"),
                                                new IProjectionConfig.ProjectionColumn("destination_longitude", "destinationLongitude"))
                                        .withConnection("destinationDeliveryAddressJoin")
                        )
                        .withOutput("orderSummaryProjection");
        }
}
