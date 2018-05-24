package com.shotgun.viewserver.setup;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.shotgun.viewserver.setup.datasource.ContentTypeDataSource;
import com.shotgun.viewserver.setup.datasource.ProductCategoryDataSource;
import com.shotgun.viewserver.setup.datasource.ProductDataSource;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseCsvDataLoader;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.adapters.mongo.MongoCsvDataLoader;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.DataSourceRegistry;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import io.viewserver.server.setup.IApplicationSetup;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 10/09/15.
 */

public class MongoApplicationSetup implements IApplicationSetup {

    private IApplicationGraphDefinitions graphDefinitions;
    private String csvDataPath;
    private static final Logger log = LoggerFactory.getLogger(MongoApplicationSetup.class);
    private JacksonSerialiser serialiser = new JacksonSerialiser();
    private MongoConnectionFactory connectionFactory;

    public MongoApplicationSetup(MongoConnectionFactory connectionFactory, IApplicationGraphDefinitions graphDefinitions, String csvDataPath) {
        this.connectionFactory = connectionFactory;
        this.graphDefinitions = graphDefinitions;
        this.csvDataPath = csvDataPath;
    }

    @Override
    public void run() {
        log.info("Bootstrapping firebase database");
        setup(connectionFactory.getConnection());
    }


    protected void setup(MongoDatabase db) {
        setupDataSources(db);
        setupReports(db);
        recreate(db, ProductDataSource.NAME, ProductDataSource.getDataSource().getSchema());
        recreate(db, ContentTypeDataSource.NAME, ContentTypeDataSource.getDataSource().getSchema());
        recreate(db, ProductCategoryDataSource.NAME, ProductCategoryDataSource.getDataSource().getSchema());
    }

    private void recreate(MongoDatabase db, String operatorName, SchemaConfig schema) {
        MongoCollection<Document> collection = db.getCollection(operatorName);
        collection.drop();
        MongoCsvDataLoader loader = new MongoCsvDataLoader(String.format("%s/%s.csv", csvDataPath,operatorName), operatorName, schema,connectionFactory);
        int noRecords = loader.load();
        log.info("{} records loading into mongo table {}",noRecords,operatorName);
    }

    private void setupDataSources(MongoDatabase db) {
        log.info("Creating data sources");
        Collection<DataSource> dataSources = graphDefinitions.getDataSources();

        try{
            //clear the datasources collection
            MongoCollection<Document> collection = db.getCollection(DataSourceRegistry.TABLE_NAME);
            collection.drop();

            for (DataSource dataSource : dataSources) {
                log.debug("-    {}", dataSource.getName());
                try {
                    String json = serialiser.serialise(dataSource, true);

                    Map<String, Object> docData = new HashMap<>();
                    docData.put("name", dataSource.getName());
                    docData.put("json", json);
                    BasicDBObject query = new BasicDBObject("_id",dataSource.getName());
                    collection.replaceOne(query, new Document(docData),new UpdateOptions().upsert(true));
                } catch (Exception e) {
                    log.error(String.format("Could not create data source '%s'", dataSource.getName()), e);
                }
            }
        }catch (Exception e) {
            log.error("Could not create data sources", e);
        }
    }

    private void setupReports(MongoDatabase db) {
        log.info("Creating report definitions");

        Map<String, ReportDefinition> reportDefinitions = graphDefinitions.getReportDefinitions();

        try{
            //clear the datasources collection
            MongoCollection<Document> collection = db.getCollection(ReportRegistry.TABLE_NAME);
            collection.drop();

            for (ReportDefinition reportDefinition : reportDefinitions.values()) {
                log.debug("-    {}", reportDefinition.getId());
                try {
                    String json = serialiser.serialise(reportDefinition, true);

                    Map<String, Object> docData = new HashMap<>();
                    docData.put("id", reportDefinition.getId());
                    docData.put("name", reportDefinition.getName());
                    docData.put("dataSource", reportDefinition.getDataSource());
                    docData.put("json", json);
                    BasicDBObject query = new BasicDBObject("_id", reportDefinition.getId());
                    collection.replaceOne(query, new Document(docData),new UpdateOptions().upsert(true));
                } catch (Exception e) {
                    log.error(String.format("Could not create report '%s'", reportDefinition.getId()), e);
                }
            }
        }catch (Exception e) {
            log.error("Could not create report definitions", e);
        }
    }
}
