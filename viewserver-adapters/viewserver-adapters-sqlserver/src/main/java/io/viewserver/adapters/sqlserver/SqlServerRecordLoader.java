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

/**
 * Created by Paul on 28/09/2015.
 */
package io.viewserver.adapters.sqlserver;

import io.viewserver.adapters.common.IWritableDataQueryProvider;
import io.viewserver.adapters.jdbc.JdbcDataAdapterBase;
import io.viewserver.util.ViewServerException;
import net.sourceforge.jtds.jdbcx.JtdsDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class SqlServerDataAdapter extends JdbcDataAdapterBase {
    private static final Logger log = LoggerFactory.getLogger(SqlServerDataAdapter.class);
    private JtdsDataSource dataSource;
    private String server;
    private Integer port;
    private String instance;
    private String database;

    public SqlServerDataAdapter(String server, Integer port, String instance, String database, String username, String password) {
        super(username, password);
        this.server = server;
        this.port = port;
        this.instance = instance;
        this.database = database;
    }

    public String getInstance() {
        return instance;
    }

    public String getServer() {
        return server;
    }

    public Integer getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    @Override
    protected DataSource createDataSource() {
        try {
            dataSource = new JtdsDataSource();
            dataSource.setServerName(server);

            if(this.instance != null) {
                dataSource.setInstance(instance);
            }else{
                dataSource.setPortNumber(port);
            }

            dataSource.setDatabaseName(database);

            //TODO - set best settings see here https://msdn.microsoft.com/en-us/library/ms378988(v=sql.110).aspx

            return dataSource;
        } catch (Throwable e) {
            throw new ViewServerException(e);
        }
    }
}
