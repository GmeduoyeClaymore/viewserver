package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.JoinColumnNamer;
import io.viewserver.execution.nodes.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.join.IColumnNameResolver;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class CustomerOrderSummaryReport {
        public static final String ID = "customerOrderSummary";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "customerOrderSummary")
                        .withDataSource(OrderDataSource.NAME)
                        .withParameter("userId", "User Id", String[].class)
                        .withParameter("isCompleted", "Is Order Complete", String[].class)
                        .withParameter("orderId", "Order Id", String[].class)
                        .withNodes(
                                new FilterNode("orderFilter")
                                        .withExpression("userId == \"{userId}\" && status != \"CANCELLED\" && if(\"{orderId}\" != \"\", orderId == \"{orderId}\", orderId != null) && if(\"{isCompleted}\" != \"\", if(\"{isCompleted}\" == \"COMPLETED\", status == \"COMPLETED\", status != \"COMPLETED\"), orderId != null)")
                                        .withConnection("#input", null, Constants.IN),
                                new JoinNode("driverJoin")
                                        .withLeftJoinColumns("driverId")
                                        .withLeftJoinOuter()
                                        .withRightJoinColumns("userId")
                                        .withColumnPrefixes("", "driver_")
                                        .withAlwaysResolveNames()
                                        .withConnection("orderFilter", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, UserDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("vehicleJoin")
                                        .withLeftJoinColumns("driverId")
                                        .withLeftJoinOuter()
                                        .withRightJoinColumns("userId")
                                      /*  .withColumnPrefixes("", "vehicle_")
                                        .withAlwaysResolveNames()*/
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
                                new ProjectionNode("orderSummaryProjection")
                                        .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                        .withProjectionColumns(
                                                new IProjectionConfig.ProjectionColumn("orderId"),
                                                new IProjectionConfig.ProjectionColumn("totalPrice"),
                                                new IProjectionConfig.ProjectionColumn("productId"),
                                                new IProjectionConfig.ProjectionColumn("contentTypeId"),
                                                new IProjectionConfig.ProjectionColumn("notes"),
                                                new IProjectionConfig.ProjectionColumn("imageUrl"),
                                                new IProjectionConfig.ProjectionColumn("paymentId"),
                                                new IProjectionConfig.ProjectionColumn("deliveryId"),
                                                new IProjectionConfig.ProjectionColumn("driverRating"),
                                                new IProjectionConfig.ProjectionColumn("registrationNumber", "registrationNumber"),
                                                new IProjectionConfig.ProjectionColumn("colour", "vehicleColour"),
                                                new IProjectionConfig.ProjectionColumn("make", "vehicleMake"),
                                                new IProjectionConfig.ProjectionColumn("model", "vehicleModel"),
                                                new IProjectionConfig.ProjectionColumn("driver_firstName", "driverFirstName"),
                                                new IProjectionConfig.ProjectionColumn("driver_lastName", "driverLastName"),
                                                new IProjectionConfig.ProjectionColumn("driver_imageUrl", "driverImageUrl"),
                                                new IProjectionConfig.ProjectionColumn("driver_latitude", "driverLatitude"),
                                                new IProjectionConfig.ProjectionColumn("driver_longitude", "driverLongitude"),
                                                new IProjectionConfig.ProjectionColumn("status"),
                                                new IProjectionConfig.ProjectionColumn("created"),
                                                new IProjectionConfig.ProjectionColumn("from"),
                                                new IProjectionConfig.ProjectionColumn("till"),
                                                new IProjectionConfig.ProjectionColumn("distance"),
                                                new IProjectionConfig.ProjectionColumn("duration"),
                                                new IProjectionConfig.ProjectionColumn("contentType_contentTypeId", "contentTypeContentTypeId"),
                                                new IProjectionConfig.ProjectionColumn("contentType_name", "contentTypeName"),
                                                new IProjectionConfig.ProjectionColumn("contentType_origin", "contentTypeOrigin"),
                                                new IProjectionConfig.ProjectionColumn("contentType_destination", "contentTypeDestination"),
                                                new IProjectionConfig.ProjectionColumn("contentType_noPeople", "contentTypeNoPeople"),
                                                new IProjectionConfig.ProjectionColumn("contentType_fromTime", "contentTypeFromTime"),
                                                new IProjectionConfig.ProjectionColumn("contentType_tillTime", "contentTypeTillTime"),
                                                new IProjectionConfig.ProjectionColumn("contentType_noItems", "contentTypeNoItems"),
                                                new IProjectionConfig.ProjectionColumn("contentType_hasVehicle", "contentTypeHasVehicle"),
                                                new IProjectionConfig.ProjectionColumn("contentType_rootProductCategory", "contentTypeRootProductCategory"),
                                                new IProjectionConfig.ProjectionColumn("contentType_pricingStrategy", "contentTypePricingStrategy"),
                                                new IProjectionConfig.ProjectionColumn("product_productId", "productProductId"),
                                                new IProjectionConfig.ProjectionColumn("product_name", "productName"),
                                                new IProjectionConfig.ProjectionColumn("product_imageUrl", "productImageUrl"),
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
