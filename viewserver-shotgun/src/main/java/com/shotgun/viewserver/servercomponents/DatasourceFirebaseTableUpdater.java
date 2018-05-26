package com.shotgun.viewserver.servercomponents;

import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseTableUpdater;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.adapters.mongo.MongoTableUpdater;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.SchemaConfig;
import rx.Observable;

public class DatasourceFirebaseTableUpdater extends FirebaseTableUpdater {
    public DatasourceFirebaseTableUpdater(FirebaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record) {
        DataSourceTableName dsTableName = new DataSourceTableName(tableName);
        return super.addOrUpdateRow(dsTableName.getDataSourceName(), schemaConfig, record);
    }
}

