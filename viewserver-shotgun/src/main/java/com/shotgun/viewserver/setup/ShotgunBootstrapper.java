package com.shotgun.viewserver.setup;

import com.shotgun.viewserver.setup.datasource.*;
import com.shotgun.viewserver.setup.report.*;
import io.viewserver.report.ReportDefinition;
import io.viewserver.server.setup.BootstrapperBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 10/09/15.
 */
public class ShotgunBootstrapper extends BootstrapperBase {
    @Override
    protected Collection<io.viewserver.datasource.DataSource> getDataSources() {
        Collection<io.viewserver.datasource.DataSource> dataSources = super.getDataSources();
        dataSources.add(UserDataSource.getDataSource());
        dataSources.add(RatingDataSource.getDataSource());
        dataSources.add(DeliveryAddressDataSource.getDataSource());
        dataSources.add(DeliveryDataSource.getDataSource());
        dataSources.add(OrderDataSource.getDataSource());
        dataSources.add(UserRelationshipDataSource.getDataSource());
        dataSources.add(OrderItemsDataSource.getDataSource());
        dataSources.add(VehicleDataSource.getDataSource());
        dataSources.add(ProductCategoryDataSource.getDataSource());
        dataSources.add(ProductDataSource.getDataSource());
        dataSources.add(ContentTypeDataSource.getDataSource());
        dataSources.add(PhoneNumberDataSource.getDataSource());
        dataSources.add(UserProductDataSource.getDataSource());
        return dataSources;
    }

    @Override
    protected Map<String, ReportDefinition> getReportDefinitions() {
        Map<String, ReportDefinition> reportDefinitions = new HashMap<>();
        reportDefinitions.put(CustomerOrderSummaryReport.ID, CustomerOrderSummaryReport.getReportDefinition());
        reportDefinitions.put(DriverOrderSummaryReport.ID, DriverOrderSummaryReport.getReportDefinition());
        reportDefinitions.put(ProductCategoryReport.ID, ProductCategoryReport.getReportDefinition());
        reportDefinitions.put(OrderRequestReport.ID, OrderRequestReport.getReportDefinition());
        reportDefinitions.put(OperatorAndConnectionReport.ID, OperatorAndConnectionReport.getReportDefinition());
        reportDefinitions.put(UserReport.ID, UserReport.getReportDefinition());
        reportDefinitions.put(UserRelationshipReport.USER_RELATIONSHIPS, UserRelationshipReport.getReportDefinition());
        reportDefinitions.put(UserRelationshipReport.USER_FOR_PRODUCT_REPORT_ID, UserRelationshipReport.getUsersForProductReportDefinition());
        reportDefinitions.put(ContentTypeReport.ID, ContentTypeReport.getReportDefinition());
        return reportDefinitions;
    }
}
