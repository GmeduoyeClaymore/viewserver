package io.viewserver.server;

import io.viewserver.datasource.IDataSourceRegistry;

public interface IDataSourceServerComponents extends IServerComponent{
    IDataSourceRegistry getDataSourceRegistry();
}
