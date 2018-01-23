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
                                        .withExpression("userId == \"{userId}\" && if(\"{orderId}\" != \"\", orderId == \"{orderId}\", orderId != null) && if(\"{isCompleted}\" != \"\", if(\"{isCompleted}\" == \"COMPLETED\", status == \"COMPLETED\", status != \"COMPLETED\"), orderId != null)")
                                        .withConnection("#input", null, Constants.IN),
                                new JoinNode("orderItemsJoin")
                                        .withLeftJoinColumns("orderId")
                                        .withRightJoinColumns("orderId")
                                        .withConnection("orderFilter", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(OrderItemsDataSource.NAME, OrderItemsDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("deliveryJoin")
                                        .withLeftJoinColumns("deliveryId")
                                        .withRightJoinColumns("deliveryId")
                                        .withConnection("orderItemsJoin", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(DeliveryDataSource.NAME, DeliveryDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("driverJoin")
                                        .withLeftJoinColumns("driverId")
                                        .withLeftJoinOuter()
                                        .withRightJoinColumns("userId")
                                        .withConnection("deliveryJoin", Constants.OUT, "left")
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
                                                new IProjectionConfig.ProjectionColumn("notes"),
                                                new IProjectionConfig.ProjectionColumn("imageUrl"),
                                                new IProjectionConfig.ProjectionColumn("paymentId"),
                                                new IProjectionConfig.ProjectionColumn("deliveryId"),
                                                new IProjectionConfig.ProjectionColumn("driverRating"),
                                                new IProjectionConfig.ProjectionColumn("registrationNumber", "registrationNumber"),
                                                new IProjectionConfig.ProjectionColumn("colour", "vehicleColour"),
                                                new IProjectionConfig.ProjectionColumn("make", "vehicleMake"),
                                                new IProjectionConfig.ProjectionColumn("model", "vehicleModel"),
                                                new IProjectionConfig.ProjectionColumn("firstName", "driverFirstName"),
                                                new IProjectionConfig.ProjectionColumn("lastName", "driverLastName"),
                                                new IProjectionConfig.ProjectionColumn("latitude", "driverLatitude"),
                                                new IProjectionConfig.ProjectionColumn("longitude", "driverLongitude"),
                                                new IProjectionConfig.ProjectionColumn("vehicleTypeId"),
                                                new IProjectionConfig.ProjectionColumn("noRequiredForOffload"),
                                                new IProjectionConfig.ProjectionColumn("status"),
                                                new IProjectionConfig.ProjectionColumn("created"),
                                                new IProjectionConfig.ProjectionColumn("eta"),
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
