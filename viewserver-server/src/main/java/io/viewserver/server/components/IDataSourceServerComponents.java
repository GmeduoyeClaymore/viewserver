package io.viewserver.server.components;

import io.viewserver.datasource.IDataSourceRegistry;

public interface IDataSourceServerComponents extends IServerComponent{
    IDataSourceRegistry getDataSourceRegistry();
}
