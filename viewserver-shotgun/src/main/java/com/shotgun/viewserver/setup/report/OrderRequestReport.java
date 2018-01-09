package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.DeliveryAddressDataSource;
import com.shotgun.viewserver.setup.datasource.DeliveryDataSource;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import com.shotgun.viewserver.setup.datasource.OrderItemsDataSource;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class OrderRequestReport {
        public static final String ID = "orderRequest";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "orderRequest")
                        .withDataSource(OrderDataSource.NAME)
                        .withParameter("vehicleTypeId", "Vehicle Type Id", String[].class)
                        .withParameter("productId", "Product Id", String[].class)
                        .withParameter("noRequiredForOffload", "Number required for offload", int[].class)
                        .withParameter("driverLatitude", "Driver Latitude", double[].class)
                        .withParameter("driverLongitude", "Driver Longitude", double[].class)
                        .withParameter("maxDistance", "Maximum Distance", String[].class)
                        .withNodes(
                                new JoinNode("deliveryJoin")
                                        .withLeftJoinColumns("deliveryId")
                                        .withRightJoinColumns("deliveryId")
                                        .withConnection("#input", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(DeliveryDataSource.NAME, DeliveryDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("orderItemJoin")
                                        .withLeftJoinColumns("orderId")
                                        .withRightJoinColumns("orderId")
                                        .withConnection("deliveryJoin", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(OrderItemsDataSource.NAME, OrderItemsDataSource.NAME), Constants.OUT, "right"),
                                new FilterNode("orderFilter")
                                      /*  .withExpression("status == \"PLACED\" && vehicleTypeId == \"{vehicleTypeId}\" && noRequiredForOffload <= {noRequiredForOffload}")*/
                                        .withExpression("productId == \"{productId}\" && status == \"PLACED\"")
                                        .withConnection("orderItemJoin"),
                                new JoinNode("originDeliveryAddressJoin")
                                        .withLeftJoinColumns("originDeliveryAddressId")
                                        .withRightJoinColumns("deliveryAddressId")
                                        .withConnection("orderFilter", Constants.OUT, "left")
                                        .withColumnPrefixes("", "origin_")
                                        .withAlwaysResolveNames()
                                        .withConnection(IDataSourceRegistry.getOperatorPath(DeliveryAddressDataSource.NAME, DeliveryAddressDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("destinationDeliveryAddressJoin")
                                        .withLeftJoinColumns("destinationDeliveryAddressId")
                                        .withLeftJoinOuter()
                                        .withRightJoinColumns("deliveryAddressId")
                                        .withConnection("originDeliveryAddressJoin", Constants.OUT, "left")
                                        .withColumnPrefixes("", "destination_")
                                        .withAlwaysResolveNames()
                                        .withConnection(IDataSourceRegistry.getOperatorPath(DeliveryAddressDataSource.NAME, DeliveryAddressDataSource.NAME), Constants.OUT, "right"),
                                new CalcColNode("distanceCalcCol")
                                        .withCalculations(new CalcColOperator.CalculatedColumn("distance", "distance(origin_latitude, origin_longitude, {driverLatitude}, {driverLongitude}, \"M\")"))
                                        .withConnection("destinationDeliveryAddressJoin"),
                                new FilterNode("distanceFilter")
                                        .withExpression("distance <= {maxDistance}")
                                        .withConnection("distanceCalcCol"),
                                new ProjectionNode("orderRequestProjection")
                                        .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                        .withProjectionColumns(
                                                new IProjectionConfig.ProjectionColumn("orderId"),
                                                new IProjectionConfig.ProjectionColumn("productId"),
                                                new IProjectionConfig.ProjectionColumn("notes"),
                                                new IProjectionConfig.ProjectionColumn("imageUrl"),
                                                new IProjectionConfig.ProjectionColumn("deliveryId"),
                                                new IProjectionConfig.ProjectionColumn("vehicleTypeId"),
                                                new IProjectionConfig.ProjectionColumn("noRequiredForOffload"),
                                                new IProjectionConfig.ProjectionColumn("status"),
                                                new IProjectionConfig.ProjectionColumn("created"),
                                                new IProjectionConfig.ProjectionColumn("eta"),
                                                new IProjectionConfig.ProjectionColumn("distance"),
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
                                        .withConnection("distanceFilter")
                        )
                        .withOutput("orderRequestProjection");
        }
}
