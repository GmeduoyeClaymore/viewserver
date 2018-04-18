package io.viewserver.datasource;

import io.viewserver.core.IExecutionContext;

public interface IDataAdapterFactory {
    IRecordLoader getAdapter(IExecutionContext context, String name, String tableName);
}
