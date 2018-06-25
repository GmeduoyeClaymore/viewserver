package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class OrderRequestReport {
        public static final String ID = "orderRequest";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "orderRequest")
                        .withDataSource(OrderDataSource.NAME)
                        .withRequiredParameter("partnerLatitude", "Partner Latitude Override", double[].class)
                        .withRequiredParameter("partnerLongitude", "Partner Longitude Override", double[].class)
                        .withRequiredParameter("maxDistance", "Maximum Distance Override", String[].class)
                        .withRequiredParameter("@userId", "User Id", String[].class)
                        //.withRequiredParameter("showOutOfRange", "Show Out Of Range", boolean[].class)
                        .withNodes(
                                new CalcColNode("distanceCalcCol")
                                        .withCalculations(
                                                new CalcColOperator.CalculatedColumn("userCreatedThisOrder", "userId == \"{@userId}\""),
                                                new CalcColOperator.CalculatedColumn("currentDistance", "distanceJson(orderLocation, isNull({partnerLatitude},partner_latitude), isNull({partnerLongitude},partner_longitude), \"M\")"),
                                                new CalcColOperator.CalculatedColumn("currentDistanceFilter", "distanceJson(orderLocation, isNull({partnerLatitude},partner_latitude), isNull({partnerLongitude},partner_longitude), \"M\")"))
                                        .withConnection("#input"),
                                new FilterNode("distanceFilter")
                                        .withExpression("currentDistanceFilter <= isNull({maxDistance}, partner_range)")
                                        .withConnection("distanceCalcCol"),
                                new FilterNode("hasResponded")
                                        .withExpression("getResponseField(\"{@userId}\",\"responseStatus\",orderDetails) == null")
                                        .withConnection("distanceFilter"),
                                new CalcColNode("orderVisibleCalc")
                                        .withCalculations(
                                                new CalcColOperator.CalculatedColumn("userCanSeeOrder", "isOrderVisible(\"{@userId}\",customer_relationships, orderDetails)"))
                                        .withConnection("hasResponded"),
                                new FilterNode("notIsBlocked")
                                        .withExpression("userCanSeeOrder")
                                        .withConnection("orderVisibleCalc"),
                                new ProjectionNode("orderRequestProjection")
                                        .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                        .withProjectionColumns(
                                                new IProjectionConfig.ProjectionColumn("customer_firstName"),
                                                new IProjectionConfig.ProjectionColumn("customer_lastName"),
                                                new IProjectionConfig.ProjectionColumn("customer_ratingAvg"),
                                                new IProjectionConfig.ProjectionColumn("partner_latitude"),
                                                new IProjectionConfig.ProjectionColumn("partner_longitude"),
                                                new IProjectionConfig.ProjectionColumn("partner_firstName"),
                                                new IProjectionConfig.ProjectionColumn("partner_lastName"),
                                                new IProjectionConfig.ProjectionColumn("partner_email"),
                                                new IProjectionConfig.ProjectionColumn("partner_imageUrl"),
                                                new IProjectionConfig.ProjectionColumn("partner_online"),
                                                new IProjectionConfig.ProjectionColumn("partner_userStatus"),
                                                new IProjectionConfig.ProjectionColumn("partner_statusMessage"),
                                                new IProjectionConfig.ProjectionColumn("partner_ratingAvg"),
                                                new IProjectionConfig.ProjectionColumn("partner_ratingAvg"),
                                                new IProjectionConfig.ProjectionColumn("orderLocation"),
                                                new IProjectionConfig.ProjectionColumn("requiredDate"),
                                                new IProjectionConfig.ProjectionColumn("totalPrice"),
                                                new IProjectionConfig.ProjectionColumn("orderContentTypeId"),
                                                new IProjectionConfig.ProjectionColumn("orderDetails"),
                                                new IProjectionConfig.ProjectionColumn("orderId"),
                                                new IProjectionConfig.ProjectionColumn("responseStatus"),
                                                new IProjectionConfig.ProjectionColumn("orderLocation"),
                                                new IProjectionConfig.ProjectionColumn("totalPrice"),
                                                new IProjectionConfig.ProjectionColumn("orderContentTypeId"),
                                                new IProjectionConfig.ProjectionColumn("orderDetails"),
                                                new IProjectionConfig.ProjectionColumn("requiredDate"),
                                                new IProjectionConfig.ProjectionColumn("lastModified"),
                                                new IProjectionConfig.ProjectionColumn("created"),
                                                new IProjectionConfig.ProjectionColumn("orderId"),
                                                new IProjectionConfig.ProjectionColumn("productName"),
                                                new IProjectionConfig.ProjectionColumn("path"),
                                                new IProjectionConfig.ProjectionColumn("contentTypeRootProductCategory"),
                                                new IProjectionConfig.ProjectionColumn("status"),
                                                new IProjectionConfig.ProjectionColumn("userCreatedThisOrder")
                                        )
                                        .withConnection("notIsBlocked")
                        )
                        .withOutput("orderRequestProjection");
        }
}
