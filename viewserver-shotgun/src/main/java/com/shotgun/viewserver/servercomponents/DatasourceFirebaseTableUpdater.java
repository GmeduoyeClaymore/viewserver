package com.shotgun.viewserver.servercomponents;

import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseTableUpdater;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.SchemaConfig;

public class DatasourceFirebaseTableUpdater extends FirebaseTableUpdater {
    public DatasourceFirebaseTableUpdater(FirebaseConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public void addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record) {
        DataSourceTableName dsTableName = new DataSourceTableName(tableName);
        super.addOrUpdateRow(dsTableName.getDataSourceName(), schemaConfig, record);
    }
}
