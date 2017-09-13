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

package io.viewserver.adapters.h2;

import io.viewserver.adapters.common.IWritableDataQueryProvider;
import io.viewserver.adapters.jdbc.JdbcDataAdapterBase;
import io.viewserver.core.Utils;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.util.ViewServerException;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 16/02/2015.
 */
public class H2DataAdapter extends JdbcDataAdapterBase {
    private String path;
    private String tableName;

    public H2DataAdapter(String path, String username, String password, IWritableDataQueryProvider dataQueryProvider, String tableName) {
        super(username, password, dataQueryProvider);
        this.path = path;
        this.tableName = tableName;
    }

    public String getPath() {
        return path;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    protected DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(String.format("jdbc:h2:%s;IFEXISTS=TRUE;MV_STORE=FALSE;DATABASE_TO_UPPER=FALSE", Utils.replaceSystemTokens(path)));
        return dataSource;
    }

    @Override
    public TableKeyDefinition getDerivedTableKeyDefinition() {
        final TableKeyDefinition[] derivedTableKeyDefinition = new TableKeyDefinition[1];
        executeQuery(String.format("show columns from %s", getTableName()), (resultSet) -> {
            List<String> keys = new ArrayList<>();
            while (resultSet.next()) {
                if ("PRI".equals(resultSet.getString(4))) {
                    keys.add(resultSet.getString(1));
                }
            }
            if (keys.isEmpty()) {
                throw new ViewServerException("Cannot derive table key definition from SQL table - no primary key exists");
            }
            derivedTableKeyDefinition[0] = new TableKeyDefinition(keys.toArray(new String[keys.size()]));
        });
        return derivedTableKeyDefinition[0];
    }
}
