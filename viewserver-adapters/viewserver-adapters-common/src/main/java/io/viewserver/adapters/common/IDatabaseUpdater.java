package io.viewserver.adapters.common;

import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.operators.table.KeyedTable;
import rx.Observable;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface IDatabaseUpdater {

    Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record, Integer version);

    default Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record, Supplier<Integer> version) {
        return addOrUpdateRow(tableName,schemaConfig,record,version == null ? null : version.get());
    }
    default void stop(){
    }
}
