package com.shotgun.viewserver.servercomponents;

import io.viewserver.datasource.IRecord;
import io.viewserver.operators.table.KeyedTable;
import rx.Observable;

public interface IDatabaseUpdater {
    void addOrUpdateRow(String tableName, String dataSourceName, IRecord record);

    void addOrUpdateRow(KeyedTable table, String dataSourceName, IRecord record);

    Observable<Boolean> scheduleAddOrUpdateRow(KeyedTable table, String dataSourceName, IRecord record);
}
