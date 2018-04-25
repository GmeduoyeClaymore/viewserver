package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class CustomerOrderSummaryReport {
    public static final String ID = "customerOrderSummary";

    public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, "customerOrderSummary")
                .withDataSource(OrderDataSource.NAME)
                .withNodes(
                        new JoinNode("driverJoin")
                                .withLeftJoinColumns("driverId")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("", "driver_")
                                .withAlwaysResolveNames()
                                .withConnection("#input", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, "ratingJoin"), Constants.OUT, "right"),
                        new JoinNode("vehicleJoin")
                                .withLeftJoinColumns("driverId")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("userId")
                                .withConnection("driverJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(VehicleDataSource.NAME, VehicleDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("originDeliveryAddressJoin")
                                .withLeftJoinColumns("originDeliveryAddressId")
                                .withRightJoinColumns("deliveryAddressId")
                                .withColumnPrefixes("", "origin_")
                                .withAlwaysResolveNames()
                                .withConnection("vehicleJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(DeliveryAddressDataSource.NAME, DeliveryAddressDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("destinationDeliveryAddressJoin")
                                .withLeftJoinColumns("destinationDeliveryAddressId")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("deliveryAddressId")
                                .withColumnPrefixes("", "destination_")
                                .withAlwaysResolveNames()
                                .withConnection("originDeliveryAddressJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(DeliveryAddressDataSource.NAME, DeliveryAddressDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("ratingJoin")
                                .withLeftJoinColumns("orderId", "driverId")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("orderId", "userId")
                                .withConnection("destinationDeliveryAddressJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(RatingDataSource.NAME, RatingDataSource.NAME), Constants.OUT, "right"),
                        new ProjectionNode("orderSummaryProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("orderId"),
                                        new IProjectionConfig.ProjectionColumn("totalPrice"),
                                        new IProjectionConfig.ProjectionColumn("productId"),
                                        new IProjectionConfig.ProjectionColumn("contentTypeId"),
                                        new IProjectionConfig.ProjectionColumn("imageUrl"),
                                        new IProjectionConfig.ProjectionColumn("notes"),
                                        new IProjectionConfig.ProjectionColumn("quantity"),
                                        new IProjectionConfig.ProjectionColumn("paymentId"),
                                        new IProjectionConfig.ProjectionColumn("deliveryId"),
                                        new IProjectionConfig.ProjectionColumn("fixedPrice"),
                                        new IProjectionConfig.ProjectionColumn("startTime"),
                                        new IProjectionConfig.ProjectionColumn("endTime"),
                                        new IProjectionConfig.ProjectionColumn("userId", "customerUserId"),
                                        new IProjectionConfig.ProjectionColumn("rating", "driverRating"),
                                        new IProjectionConfig.ProjectionColumn("registrationNumber", "registrationNumber"),
                                        new IProjectionConfig.ProjectionColumn("colour", "vehicleColour"),
                                        new IProjectionConfig.ProjectionColumn("make", "vehicleMake"),
                                        new IProjectionConfig.ProjectionColumn("model", "vehicleModel"),
                                        new IProjectionConfig.ProjectionColumn("driver_ratingAvg", "driverRatingAvg"),
                                        new IProjectionConfig.ProjectionColumn("driver_firstName", "driverFirstName"),
                                        new IProjectionConfig.ProjectionColumn("driver_lastName", "driverLastName"),
                                        new IProjectionConfig.ProjectionColumn("driver_imageUrl", "driverImageUrl"),
                                        new IProjectionConfig.ProjectionColumn("driver_latitude", "driverLatitude"),
                                        new IProjectionConfig.ProjectionColumn("driver_longitude", "driverLongitude"),
                                        new IProjectionConfig.ProjectionColumn("status"),
                                        new IProjectionConfig.ProjectionColumn("created"),
                                        new IProjectionConfig.ProjectionColumn("distance"),
                                        new IProjectionConfig.ProjectionColumn("duration"),
                                        new IProjectionConfig.ProjectionColumn("contentType_contentTypeId", "contentTypeContentTypeId"),
                                        new IProjectionConfig.ProjectionColumn("contentType_name", "contentTypeName"),
                                        new IProjectionConfig.ProjectionColumn("contentType_hasOrigin", "contentTypeHasOrigin"),
                                        new IProjectionConfig.ProjectionColumn("contentType_hasDestination", "contentTypeHasDestination"),
                                        new IProjectionConfig.ProjectionColumn("contentType_hasStartTime", "contentTypeHasStartTime"),
                                        new IProjectionConfig.ProjectionColumn("contentType_hasEndTime", "contentTypeHasEndTime"),
                                        new IProjectionConfig.ProjectionColumn("contentType_rootProductCategory", "contentTypeRootProductCategory"),
                                        new IProjectionConfig.ProjectionColumn("contentType_pricingStrategy", "contentTypePricingStrategy"),
                                        new IProjectionConfig.ProjectionColumn("product_productId", "productProductId"),
                                        new IProjectionConfig.ProjectionColumn("product_name", "productName"),
                                        new IProjectionConfig.ProjectionColumn("product_imageUrl", "productImageUrl"),
                                        new IProjectionConfig.ProjectionColumn("productCategory_path", "path"),
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
                                .withConnection("ratingJoin")
                )
                .withOutput("orderSummaryProjection");
    }
}
