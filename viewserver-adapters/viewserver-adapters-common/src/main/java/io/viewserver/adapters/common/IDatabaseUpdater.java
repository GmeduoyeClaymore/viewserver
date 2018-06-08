package io.viewserver.adapters.common;

import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.operators.table.KeyedTable;
import rx.Observable;

public interface IDatabaseUpdater {
    default Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record){
        return addOrUpdateRow(tableName,schemaConfig,record,record.getInt("version"));
    }

    Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record, Integer version);
}
