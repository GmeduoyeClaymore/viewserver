package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class DriverOrderSummaryReport {
        public static final String ID = "driverOrderSummary";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "driverOrderSummary")
                        .withDataSource(OrderDataSource.NAME)
                        .withParameter("userId", "User Id", String[].class)
                        .withParameter("isCompleted", "Is Order Complete", String[].class)
                        .withParameter("orderId", "Order Id", String[].class)
                        .withNodes(
                                new FilterNode("orderFilter")
                                        .withExpression("status != \"CANCELLED\" && if(\"{orderId}\" != \"\", orderId == \"{orderId}\", orderId != null) && if(\"{isCompleted}\" != \"\", if(\"{isCompleted}\" == \"COMPLETED\", status == \"COMPLETED\", status != \"COMPLETED\"), orderId != null)")
                                        .withConnection("#input", null, Constants.IN),
                                new JoinNode("customerJoin")
                                        .withLeftJoinColumns("userId")
                                        .withRightJoinColumns("userId")
                                        .withConnection("orderFilter", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, UserDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("orderItemsJoin")
                                        .withLeftJoinColumns("orderId")
                                        .withRightJoinColumns("orderId")
                                        .withConnection("customerJoin", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(OrderItemsDataSource.NAME, OrderItemsDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("productJoin")
                                        .withLeftJoinColumns("productId")
                                        .withRightJoinColumns("productId")
                                        .withColumnPrefixes("", "product_")
                                        .withAlwaysResolveNames()
                                        .withConnection("orderItemsJoin", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(ProductDataSource.NAME, ProductDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("contentTypeJoin")
                                        .withLeftJoinColumns("contentTypeId")
                                        .withRightJoinColumns("contentTypeId")
                                        .withAlwaysResolveNames()
                                        .withColumnPrefixes("", "contentType_")
                                        .withConnection("productJoin", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(ContentTypeDataSource.NAME, ContentTypeDataSource.NAME), Constants.OUT, "right"),
                                new JoinNode("deliveryJoin")
                                        .withLeftJoinColumns("deliveryId")
                                        .withRightJoinColumns("deliveryId")
                                        .withConnection("contentTypeJoin", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(DeliveryDataSource.NAME, DeliveryDataSource.NAME), Constants.OUT, "right"),
                                new FilterNode("driverIdFilter")
                                        .withExpression("if(\"{userId}\" != \"\", driverId == \"{userId}\", orderId != null)")
                                        .withConnection("deliveryJoin"),
                                new JoinNode("originDeliveryAddressJoin")
                                        .withLeftJoinColumns("originDeliveryAddressId")
                                        .withRightJoinColumns("deliveryAddressId")
                                        .withColumnPrefixes("", "origin_")
                                        .withAlwaysResolveNames()
                                        .withConnection("driverIdFilter", Constants.OUT, "left")
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
                                                new IProjectionConfig.ProjectionColumn("paymentId"),
                                                new IProjectionConfig.ProjectionColumn("deliveryId"),
                                                new IProjectionConfig.ProjectionColumn("customerRating"),
                                                new IProjectionConfig.ProjectionColumn("firstName", "customerFirstName"),
                                                new IProjectionConfig.ProjectionColumn("lastName", "customerLastName"),
                                                new IProjectionConfig.ProjectionColumn("vehicleTypeId"),
                                                new IProjectionConfig.ProjectionColumn("noRequiredForOffload"),
                                                new IProjectionConfig.ProjectionColumn("status"),
                                                new IProjectionConfig.ProjectionColumn("created"),
                                                new IProjectionConfig.ProjectionColumn("from"),
                                                new IProjectionConfig.ProjectionColumn("till"),
                                                new IProjectionConfig.ProjectionColumn("distance"),
                                                new IProjectionConfig.ProjectionColumn("duration"),
                                                new IProjectionConfig.ProjectionColumn("product_productId", "productProductId"),
                                                new IProjectionConfig.ProjectionColumn("product_name", "productName"),
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
                                                new IProjectionConfig.ProjectionColumn("contentType_defaultProductId", "defaultProductId"),
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
