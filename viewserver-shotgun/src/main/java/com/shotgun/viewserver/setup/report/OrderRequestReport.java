package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
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
                        .withParameter("noRequiredForOffload", "Number required for offload", int[].class) // not used ?
                        .withParameter("driverLatitude", "Driver Latitude Override", double[].class)
                        .withParameter("driverLongitude", "Driver Longitude Override", double[].class)
                        .withParameter("maxDistance", "Maximum Distance Override", String[].class)
                        .withParameter("showOutOfRange", "Show Out Of Range", boolean[].class)
                        .withNodes(
                                new FilterNode("orderFilter")
                                        .withExpression("status == \"PLACED\"")
                                        .withConnection("#input", null, Constants.IN),
                                new JoinNode("userJoin")
                                        .withLeftJoinColumns("userId")
                                        .withRightJoinColumns("userId")
                                        .withConnection("orderFilter", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, UserDataSource.NAME), Constants.OUT, "right")
                                        .withColumnPrefixes("", "driver_")
                                        .withAlwaysResolveNames(),
                                new JoinNode("originDeliveryAddressJoin")
                                        .withLeftJoinColumns("originDeliveryAddressId")
                                        .withRightJoinColumns("deliveryAddressId")
                                        .withConnection("userJoin", Constants.OUT, "left")
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
                                        .withCalculations(
                                                new CalcColOperator.CalculatedColumn("currentDistance", "distance(origin_latitude, origin_longitude, isNull({driverLatitude},driver_latitude), isNull({driverLongitude},driver_longitude), \"M\")"),
                                                new CalcColOperator.CalculatedColumn("currentDistanceFilter", "if({showOutOfRange},0,distance(origin_latitude, origin_longitude, isNull({driverLatitude},driver_latitude), isNull({driverLongitude},driver_longitude), \"M\"))"))
                                        .withConnection("destinationDeliveryAddressJoin"),
                                new FilterNode("distanceFilter")
                                        .withExpression("currentDistanceFilter <= isNull({maxDistance},driver_range)")
                                        .withConnection("distanceCalcCol"),
                                new ProjectionNode("orderRequestProjection")
                                        .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                        .withProjectionColumns(
                                                new IProjectionConfig.ProjectionColumn("orderId"),
                                                new IProjectionConfig.ProjectionColumn("totalPrice"),
                                                new IProjectionConfig.ProjectionColumn("productId"),
                                                new IProjectionConfig.ProjectionColumn("notes"),
                                                new IProjectionConfig.ProjectionColumn("imageUrl"),
                                                new IProjectionConfig.ProjectionColumn("deliveryId"),
                                                new IProjectionConfig.ProjectionColumn("noRequiredForOffload"),
                                                new IProjectionConfig.ProjectionColumn("status"),
                                                new IProjectionConfig.ProjectionColumn("created"),
                                                new IProjectionConfig.ProjectionColumn("from"),
                                                new IProjectionConfig.ProjectionColumn("till"),
                                                new IProjectionConfig.ProjectionColumn("distance"),
                                                new IProjectionConfig.ProjectionColumn("duration"),
                                                new IProjectionConfig.ProjectionColumn("currentDistance"),
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
                                                new IProjectionConfig.ProjectionColumn("destination_longitude", "destinationLongitude"),
                                                new IProjectionConfig.ProjectionColumn("contentType_contentTypeId", "contentTypeContentTypeId"),
                                                new IProjectionConfig.ProjectionColumn("contentType_name", "contentTypeName"),
                                                new IProjectionConfig.ProjectionColumn("contentType_origin", "contentTypeOrigin"),
                                                new IProjectionConfig.ProjectionColumn("contentType_destination", "contentTypeDestination"),
                                                new IProjectionConfig.ProjectionColumn("contentType_noPeople", "contentTypeNoPeople"),
                                                new IProjectionConfig.ProjectionColumn("contentType_fromTime", "contentTypeFromTime"),
                                                new IProjectionConfig.ProjectionColumn("contentType_tillTime", "contentTypeTillTime"),
                                                new IProjectionConfig.ProjectionColumn("productCategory_path", "path"),
                                                new IProjectionConfig.ProjectionColumn("product_productId", "productProductId"),
                                                new IProjectionConfig.ProjectionColumn("product_name", "productName"),
                                                new IProjectionConfig.ProjectionColumn("product_imageUrl", "productImageUrl"),
                                                new IProjectionConfig.ProjectionColumn("contentType_noItems", "contentTypeNoItems"),
                                                new IProjectionConfig.ProjectionColumn("contentType_hasVehicle", "contentTypeHasVehicle"),
                                                new IProjectionConfig.ProjectionColumn("contentType_rootProductCategory", "contentTypeRootProductCategory"),
                                                new IProjectionConfig.ProjectionColumn("contentType_pricingStrategy", "contentTypePricingStrategy")
                                        )
                                        .withConnection("distanceFilter")
                        )
                        .withOutput("orderRequestProjection");
        }
}
