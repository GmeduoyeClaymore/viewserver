package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.DeliveryAddressDataSource;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import com.shotgun.viewserver.setup.datasource.UserProductDataSource;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class UserProductReport {
        public static final String ID = "usersForProduct";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "usersForProduct")
                        .withDataSource(UserProductDataSource.NAME)
                        .withParameter("productId", "Product ID", String[].class) // not used ?
                        .withParameter("latitude", "User Latitude", double[].class)
                        .withParameter("longitude", "User Longitude", double[].class)
                        .withParameter("maxDistance", "Maximum Distance", String[].class)
                        .withNodes(
                                new FilterNode("productFilter")
                                        .withExpression("product_productId == \"{productId}\"")
                                        .withConnection("#input", null, Constants.IN),
                                new CalcColNode("distanceCalcCol")
                                        .withCalculations(new CalcColOperator.CalculatedColumn("currentDistance", "distance(latitude, longitude, {latitude}, {longitude}, \"M\")"))
                                        .withConnection("productFilter"),
                                new FilterNode("distanceFilter")
                                        .withExpression("currentDistance <= {maxDistance}")
                                        .withConnection("distanceCalcCol"),
                                new CalcColNode("distanceCalcColString")
                                        .withCalculations(
                                                new CalcColOperator.CalculatedColumn("distance", "text(currentDistance)"),
                                                new CalcColOperator.CalculatedColumn("latitudeText", "text(latitude)"),
                                                new CalcColOperator.CalculatedColumn("longitudeText", "text(longitude)")
                                        )
                                        .withConnection("distanceFilter"),
                                new GroupByNode("uniqueUserGroupBy")
                                        .withGroupByColumns("imageUrl","longitudeText","latitudeText","firstName","lastName","userId","distance")
                                        .withConnection("distanceCalcColString")


                        )
                        .withOutput("uniqueUserGroupBy");
        }
}
