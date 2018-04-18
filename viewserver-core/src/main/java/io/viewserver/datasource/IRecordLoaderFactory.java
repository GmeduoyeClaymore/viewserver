package io.viewserver.datasource;

import io.viewserver.core.IExecutionContext;

public interface IRecordLoaderFactory {
    IRecordLoader getRecordLoader(String spec);
}
