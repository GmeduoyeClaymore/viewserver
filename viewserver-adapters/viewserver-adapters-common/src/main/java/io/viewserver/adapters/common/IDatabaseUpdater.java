package io.viewserver.adapters.common;

import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.operators.table.KeyedTable;
import rx.Observable;

public interface IDatabaseUpdater {
    void addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record);

    Observable<Boolean> scheduleAddOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record);
}
