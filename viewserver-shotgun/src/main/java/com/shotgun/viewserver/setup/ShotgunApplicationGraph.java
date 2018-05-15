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
        dataSources.add(DeliveryAddressDataSource.getDataSource());
        dataSources.add(OrderDataSource.getDataSource());
        dataSources.add(MessagesDataSource.getDataSource());
        dataSources.add(UserRelationshipDataSource.getDataSource());
        dataSources.add(OrderWithResponseDataSource.getDataSource());
        dataSources.add(VehicleDataSource.getDataSource());
        dataSources.add(PaymentDataSource.getDataSource());

        return dataSources;
    }

    @Override
    public Map<String, ReportDefinition> getReportDefinitions() {
        Map<String, ReportDefinition> reportDefinitions = new HashMap<>();
        reportDefinitions.put(CustomerOrderSummaryReport.ID, CustomerOrderSummaryReport.getReportDefinition());
        reportDefinitions.put(PartnerOrderSummaryReport.ID, PartnerOrderSummaryReport.getReportDefinition());
        reportDefinitions.put(NotificationsReport.ID, NotificationsReport.getReportDefinition());
        reportDefinitions.put(ProductCategoryReport.ID, ProductCategoryReport.getReportDefinition());
        reportDefinitions.put(OrderRequestReport.ID, OrderRequestReport.getReportDefinition());
        reportDefinitions.put(OperatorAndConnectionReport.ID, OperatorAndConnectionReport.getReportDefinition());
        reportDefinitions.put(UserReport.ID, UserReport.getReportDefinition());
        reportDefinitions.put(ProductReport.ID, ProductReport.getReportDefinition());
        reportDefinitions.put(PaymentsReport.ID, PaymentsReport.getReportDefinition());
        reportDefinitions.put(OrderResponseReport.ID, OrderResponseReport.getReportDefinition());
        /*reportDefinitions.put(LegacyUserRelationshipReport.USER_RELATIONSHIPS, LegacyUserRelationshipReport.getReportDefinition(false));
        reportDefinitions.put(LegacyUserRelationshipReport.USER_RELATIONSHIPS + "All", LegacyUserRelationshipReport.getReportDefinition(true));
        reportDefinitions.put(LegacyUserRelationshipReport.USER_FOR_PRODUCT_REPORT_ID, LegacyUserRelationshipReport.getUsersForProductReportDefinition(false));
        reportDefinitions.put(LegacyUserRelationshipReport.USER_FOR_PRODUCT_REPORT_ID + "All", LegacyUserRelationshipReport.getUsersForProductReportDefinition(true));*/
        reportDefinitions.put(UserProductRelationshipReport.ID, UserProductRelationshipReport.getReportDefinition());
        reportDefinitions.put(UserRelationshipReport.ID, UserRelationshipReport.getReportDefinition());
        reportDefinitions.put(ContentTypeReport.ID, ContentTypeReport.getReportDefinition());
        return reportDefinitions;
    }



}
