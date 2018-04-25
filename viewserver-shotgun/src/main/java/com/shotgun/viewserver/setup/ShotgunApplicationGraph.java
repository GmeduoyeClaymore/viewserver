package com.shotgun.viewserver.setup;

import com.shotgun.viewserver.setup.datasource.*;
import com.shotgun.viewserver.setup.report.*;
import io.viewserver.datasource.DataSource;
import io.viewserver.report.ReportDefinition;
import io.viewserver.server.setup.IApplicationGraphDefinitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShotgunApplicationGraph implements IApplicationGraphDefinitions {

    @Override
    public List<DataSource> getDataSources() {

        List<io.viewserver.datasource.DataSource> dataSources = new ArrayList<>();

        //DataSources which require data from csv in all modes
        dataSources.add(ProductCategoryDataSource.getDataSource());
        dataSources.add(ProductDataSource.getDataSource());
        dataSources.add(ContentTypeDataSource.getDataSource());
        dataSources.add(PhoneNumberDataSource.getDataSource());
        dataSources.add(UserProductDataSource.getDataSource());

        //DataSources which require data from csv only in test mode
        dataSources.add(UserDataSource.getDataSource());
        dataSources.add(RatingDataSource.getDataSource());
        dataSources.add(DeliveryAddressDataSource.getDataSource());
        dataSources.add(DeliveryDataSource.getDataSource());
        dataSources.add(OrderDataSource.getDataSource());
        dataSources.add(UserRelationshipDataSource.getDataSource());
        dataSources.add(OrderItemsDataSource.getDataSource());
        dataSources.add(OrderWithResponseDataSource.getDataSource());
        dataSources.add(OrderWithPartnerDataSource.getDataSource());
        dataSources.add(VehicleDataSource.getDataSource());

        return dataSources;
    }

    @Override
    public Map<String, ReportDefinition> getReportDefinitions() {
        Map<String, ReportDefinition> reportDefinitions = new HashMap<>();
        reportDefinitions.put(CustomerOrderSummaryReport.ID, CustomerOrderSummaryReport.getReportDefinition());
        reportDefinitions.put(DriverOrderSummaryReport.ID, DriverOrderSummaryReport.getReportDefinition());
        reportDefinitions.put(ProductCategoryReport.ID, ProductCategoryReport.getReportDefinition());
        reportDefinitions.put(OrderRequestReport.ID, OrderRequestReport.getReportDefinition());
        reportDefinitions.put(OperatorAndConnectionReport.ID, OperatorAndConnectionReport.getReportDefinition());
        reportDefinitions.put(UserReport.ID, UserReport.getReportDefinition());
        reportDefinitions.put(OrderResponseReport.ID, OrderResponseReport.getReportDefinition());
        reportDefinitions.put(UserRelationshipReport.USER_RELATIONSHIPS, UserRelationshipReport.getReportDefinition(false));
        reportDefinitions.put(UserRelationshipReport.USER_RELATIONSHIPS + "All", UserRelationshipReport.getReportDefinition(true));
        reportDefinitions.put(UserRelationshipReport.USER_FOR_PRODUCT_REPORT_ID, UserRelationshipReport.getUsersForProductReportDefinition(false));
        reportDefinitions.put(UserRelationshipReport.USER_FOR_PRODUCT_REPORT_ID + "All", UserRelationshipReport.getUsersForProductReportDefinition(true));
        reportDefinitions.put(ContentTypeReport.ID, ContentTypeReport.getReportDefinition());
        return reportDefinitions;
    }



}
