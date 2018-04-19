/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.datasource;

import io.viewserver.catalog.ICatalog;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import rx.Observable;

import java.util.Collection;

/**
 * Created by nick on 18/02/2015.
 */
public interface IDataSourceRegistry{
    String TABLE_NAME = "datasources";
    String ID_COL = "name";
    String JSON_COL = "json";
    String STATUS_COL = "status";
    String PATH_COL = "path";

    static String getOperatorPath(String dataSourceName, String operatorName) {
        return String.format("/%s/%s/%s", TABLE_NAME, dataSourceName, operatorName);
    }

    static String getDefaultOperatorPath(String dataSourceName) {
        return String.format("/%s/%s/%s", TABLE_NAME, dataSourceName, DataSource.DEFAUT_NAME);
    }

    static String getDefaultOperatorPath(IDataSource dataSource, String operatorName) {
        return getOperatorPath(dataSource.getName(), operatorName);
    }

    static String getTablePath(IDataSource dataSource) {
        return String.format("/%s/%s/%s", TABLE_NAME, dataSource.getName(), dataSource.getName());
    }

    static ICatalog getDataSourceCatalog(IDataSource dataSource, ICatalog systemCatalog) {
        return systemCatalog.getChild(TABLE_NAME).getChild(dataSource.getName());
    }

    Observable<IDataSource> getRegistered();

    Observable<IDataSource> getStatusChanged();

    void register(IDataSource dataSource);

    void setStatus(String name, DataSourceStatus status);

    DataSourceStatus getStatus(String name);

    IDataSource get(String name);

    Collection<IDataSource> getAll();

    void clear();

    void onDataSourceBuilt(DataSourceExecutionPlanContext dataSourceExecutionPlanContext);
}
