package com.shotgun.viewserver.setup;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;
import com.google.common.io.Resources;
import com.shotgun.viewserver.IShotgunViewServerConfiguration;
import com.shotgun.viewserver.setup.datasource.*;
import com.shotgun.viewserver.setup.report.*;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.core.Utils;
import io.viewserver.datasource.DataSource;
import io.viewserver.report.ReportDefinition;
import io.viewserver.server.IViewServerMasterConfiguration;
import io.viewserver.server.setup.BootstrapperBase;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 10/09/15.
 */
public class ShotgunBootstrapper extends BootstrapperBase {
    private static final Logger log = LoggerFactory.getLogger(ShotgunBootstrapper.class);
    private IShotgunViewServerConfiguration shotgunConfiguration;

    @Override
    protected Collection<io.viewserver.datasource.DataSource> getDataSources() {
        Collection<io.viewserver.datasource.DataSource> dataSources = super.getDataSources();

        //DataSources which require data from csv in all modes
        dataSources.add(ProductCategoryDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(ProductDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(ContentTypeDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(PhoneNumberDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(UserProductDataSource.getDataSource(shotgunConfiguration));

        //DataSources which require data from csv only in test mode
        dataSources.add(UserDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(RatingDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(DeliveryAddressDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(DeliveryDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(OrderDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(UserRelationshipDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(OrderItemsDataSource.getDataSource(shotgunConfiguration));
        dataSources.add(VehicleDataSource.getDataSource(shotgunConfiguration));

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
        reportDefinitions.put(UserRelationshipReport.USER_RELATIONSHIPS, UserRelationshipReport.getReportDefinition(false));
        reportDefinitions.put(UserRelationshipReport.USER_RELATIONSHIPS + "All", UserRelationshipReport.getReportDefinition(true));
        reportDefinitions.put(UserRelationshipReport.USER_FOR_PRODUCT_REPORT_ID, UserRelationshipReport.getUsersForProductReportDefinition(false));
        reportDefinitions.put(UserRelationshipReport.USER_FOR_PRODUCT_REPORT_ID + "All", UserRelationshipReport.getUsersForProductReportDefinition(true));
        reportDefinitions.put(ContentTypeReport.ID, ContentTypeReport.getReportDefinition());
        return reportDefinitions;
    }

    @Override
    public void run(IViewServerMasterConfiguration configuration) {
        log.info("Bootstrapping local database");
        shotgunConfiguration = (IShotgunViewServerConfiguration) configuration;

        if(!shotgunConfiguration.isMock()) {
            FirebaseConnectionFactory firebaseConnectionFactory = new FirebaseConnectionFactory(shotgunConfiguration.getFirebaseKeyPath());
            setup(firebaseConnectionFactory.getConnection());
        }else{
            super.run(configuration);
        }
    }

    private void setup(Firestore db) {
        setupDataSources(db);
        setupReports(db);
    }

    private void setupDataSources(Firestore db) {
        log.info("Creating data sources");
        Collection<DataSource> dataSources = getDataSources();

        try{
            //clear the datasources collection
            CollectionReference collection = db.collection("datasources");
            FirebaseUtils.deleteCollection(collection);
            WriteBatch batch = db.batch();

            for (DataSource dataSource : dataSources) {
                log.debug("-    {}", dataSource.getName());
                try {
                    String json = serialiser.serialise(dataSource, true);

                    Map<String, Object> docData = new HashMap<>();
                    docData.put("name", dataSource.getName());
                    docData.put("json", json);
                    batch.set(collection.document(dataSource.getName()), docData);
                } catch (Exception e) {
                    log.error(String.format("Could not create data source '%s'", dataSource.getName()), e);
                }
            }
            //TODO - add result callback here
            batch.commit().get();
        }catch (Exception e) {
            log.error("Could not create data sources", e);
        }
    }

    private void setupReports(Firestore db) {
        log.info("Creating report definitions");

        Map<String, ReportDefinition> reportDefinitions = getReportDefinitions();

        try{
            //clear the datasources collection
            CollectionReference collection = db.collection("reports");
            FirebaseUtils.deleteCollection(collection);
            WriteBatch batch = db.batch();

            for (ReportDefinition reportDefinition : reportDefinitions.values()) {
                log.debug("-    {}", reportDefinition.getId());
                try {
                    String json = serialiser.serialise(reportDefinition, true);

                    Map<String, Object> docData = new HashMap<>();
                    docData.put("id", reportDefinition.getId());
                    docData.put("name", reportDefinition.getName());
                    docData.put("dataSource", reportDefinition.getDataSource());
                    docData.put("json", json);
                    batch.set(collection.document(reportDefinition.getId()), docData);
                } catch (Exception e) {
                    log.error(String.format("Could not create report '%s'", reportDefinition.getId()), e);
                }
            }
            //TODO - add result callback here
            batch.commit().get();
        }catch (Exception e) {
            log.error("Could not create report definitions", e);
        }
    }
}
