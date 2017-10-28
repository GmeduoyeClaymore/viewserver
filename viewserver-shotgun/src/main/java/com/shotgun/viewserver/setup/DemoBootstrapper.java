package com.shotgun.viewserver.setup;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.report.ReportDefinition;
import io.viewserver.server.setup.BootstrapperBase;

import java.util.Collection;
import java.util.Map;

/**
 * Created by nick on 10/09/15.
 */
public class DemoBootstrapper extends BootstrapperBase {
    @Override
    protected Collection<io.viewserver.datasource.DataSource> getDataSources() {
        Collection<io.viewserver.datasource.DataSource> dataSources = super.getDataSources();
        dataSources.add(CustomerDataSource.getDataSource());
        dataSources.add(CustomerDeliveryAddressDataSource.getDataSource());
        dataSources.add(DriverDataSource.getDataSource());
        dataSources.add(MerchantDataSource.getDataSource());
        dataSources.add(PaymentCardsDataSource.getDataSource());
        dataSources.add(DeliveryDataSource.getDataSource());
        dataSources.add(MerchantProductInventoryDataSource.getDataSource());
        dataSources.add(OrderDataSource.getDataSource());
        dataSources.add(OrderItemsDataSource.getDataSource());
        dataSources.add(OrderFulfillmentDataSource.getDataSource());
        dataSources.add(PackageTypeDataSource.getDataSource());
        dataSources.add(ProductDataSource.getDataSource());
        dataSources.add(ProductCategoryDataSource.getDataSource());
        dataSources.add(StatusDataSource.getDataSource());
        return dataSources;
    }

    @Override
    protected Map<String, ReportDefinition> getReportDefinitions() {
        Map<String, ReportDefinition> reportDefinitions = super.getReportDefinitions();
        reportDefinitions.put("orderItemByCustomerId", new ReportDefinition("orderItemByCustomerId", "orderItemByCustomerId")
                .withDataSource("orderItem")
                .withParameter("customerId","customerId",String.class)
                .withNodes(
                        new FilterNode("filterByCustomerId")
                                .withExpression("customerId == \"{customerId}\"")
                                .withConnection("#input", null, "in"))
                .withOutput("filterByCustomerId"));

        reportDefinitions.put("orderItemTotalsByCustomerId", new ReportDefinition("orderItemTotalsByCustomerId", "orderItemTotalsByCustomerId")
                .withDataSource("orderItem")
                .withParameter("customerId", "customerId", String.class)
                .withNodes(
                        new FilterNode("filterByCustomerId")
                                .withExpression("customerId == \"{customerId}\"")
                                .withConnection("#input", null, "in"),
                        new GroupByNode("sumQuantityGroupBy")
                                .withSummary("sumQuantity", "sum","quantity")
                                .withConnection("filterByCustomerId", "out", "in"))
                .withOutput("sumQuantityGroupBy"));
        return reportDefinitions;
    }
}
