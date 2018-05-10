package io.viewserver.server.components;

import io.viewserver.datasource.IDataSourceRegistry;
import rx.Observable;

public interface IDataSourceServerComponents extends IServerComponent{
    IDataSourceRegistry getDataSourceRegistry();

    Observable<Object> onDataSourcesBuilt(String... toWaitFor);
}
