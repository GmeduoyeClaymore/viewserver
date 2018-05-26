package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.adapters.mongo.MongoTableUpdater;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.util.dynamic.NamedThreadFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DatasourceMongoTableUpdater extends MongoTableUpdater {
    public static Executor MongoPersistenceExecutor = Executors.newFixedThreadPool(5,new NamedThreadFactory("mongo-persistence"));

    public DatasourceMongoTableUpdater(MongoConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record){
        DataSourceTableName dsTableName = new DataSourceTableName(tableName);
        return super.addOrUpdateRow(dsTableName.getDataSourceName(), schemaConfig, record).subscribeOn(Schedulers.from(MongoPersistenceExecutor));
    }
}
