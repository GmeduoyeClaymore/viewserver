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
import io.viewserver.adapters.common.Record;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseCsvDataLoader;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.adapters.mongo.MongoCsvDataLoader;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.*;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import io.viewserver.server.setup.IApplicationSetup;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;

/**
 * Created by nick on 10/09/15.
 */

public class MongoApplicationSetup implements IApplicationSetup {

    private String csvDataPath;
    private static final Logger log = LoggerFactory.getLogger(MongoApplicationSetup.class);
    private MongoConnectionFactory connectionFactory;

    public MongoApplicationSetup(MongoConnectionFactory connectionFactory, String csvDataPath) {
        this.connectionFactory = connectionFactory;
        this.csvDataPath = csvDataPath;
    }

    @Override
    public void run(boolean complete) {
        log.info("Bootstrapping firebase database");
        setup(connectionFactory.getConnection(), complete);
    }


    protected void setup(MongoDatabase db,boolean complete) {
        if(complete) {
            recreate(db, ProductDataSource.NAME, ProductDataSource.getDataSource().getSchema());
            recreate(db, ContentTypeDataSource.NAME, ContentTypeDataSource.getDataSource().getSchema());
            recreate(db, ProductCategoryDataSource.NAME, ProductCategoryDataSource.getDataSource().getSchema());
        }
    }

    protected void recreate(MongoDatabase db, String operatorName, SchemaConfig schema) {
        MongoCollection<Document> collection = db.getCollection(operatorName);
        collection.drop();
        MongoCsvDataLoader loader = new MongoCsvDataLoader(String.format("%s/%s.csv", csvDataPath,operatorName), operatorName, schema,connectionFactory);
        int noRecords = loader.load();
        log.info("{} records loading into mongo table {}",noRecords,operatorName);
    }

}


