package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.adapters.mongo.MongoTableUpdater;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.SchemaConfig;

public class DatasourceMongoTableUpdater extends MongoTableUpdater {
    public DatasourceMongoTableUpdater(MongoConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public void addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record) {
        DataSourceTableName dsTableName = new DataSourceTableName(tableName);
        ControllerUtils.BackgroundExecutor.execute(() -> super.addOrUpdateRow(dsTableName.getDataSourceName(), schemaConfig, record));
    }
}
