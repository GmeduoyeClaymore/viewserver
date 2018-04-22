package com.shotgun.viewserver.setup;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.DataSource;
import io.viewserver.report.ReportDefinition;
import io.viewserver.server.setup.H2ApplicationSetup;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import io.viewserver.server.setup.IApplicationSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 10/09/15.
 */

public class FirebaseApplicationSetup implements IApplicationSetup {

    private IApplicationGraphDefinitions graphDefinitions;
    private static final Logger log = LoggerFactory.getLogger(FirebaseApplicationSetup.class);
    private JacksonSerialiser serialiser = new JacksonSerialiser();
    private FirebaseConnectionFactory connectionFactory;

    public FirebaseApplicationSetup(FirebaseConnectionFactory connectionFactory, IApplicationGraphDefinitions graphDefinitions) {
        this.connectionFactory = connectionFactory;
        this.graphDefinitions = graphDefinitions;
    }

    @Override
    public void run() {
        log.info("Bootstrapping firebase database");
        setup(connectionFactory.getConnection());
    }


    private void setup(Firestore db) {
        setupDataSources(db);
        setupReports(db);
    }

    private void setupDataSources(Firestore db) {
        log.info("Creating data sources");
        Collection<DataSource> dataSources = graphDefinitions.getDataSources();

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

        Map<String, ReportDefinition> reportDefinitions = graphDefinitions.getReportDefinitions();

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
