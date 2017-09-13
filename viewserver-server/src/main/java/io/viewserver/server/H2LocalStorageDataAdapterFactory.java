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

package io.viewserver.server;

import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.common.IWritableDataQueryProvider;
import io.viewserver.adapters.common.sql.SimpleSqlDataQueryProvider;
import io.viewserver.adapters.h2.H2DataAdapter;
import io.viewserver.core.Utils;
import io.viewserver.datasource.IDataLoader;
import io.viewserver.datasource.ILocalStorageDataAdapterFactory;
import org.h2.jdbc.JdbcSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by nick on 17/02/2015.
 */
public class H2LocalStorageDataAdapterFactory implements ILocalStorageDataAdapterFactory {
    private static final Logger log = LoggerFactory.getLogger(H2LocalStorageDataAdapterFactory.class);
    private final String path;

    public H2LocalStorageDataAdapterFactory(String path) {
        this.path = path;
        log.info("Using local H2 database at {}", path);
    }

    @Override
    public IDataLoader getAdapter(String name, String tableName, int batchSize) {
        SimpleSqlDataQueryProvider dataQueryProvider = new SimpleSqlDataQueryProvider(tableName);
        return new DataLoader(
                name,
                new InstallingH2DataAdapter(path, "", "", dataQueryProvider, tableName),
                dataQueryProvider
        ).withBatchSize(batchSize);
    }

    public static class InstallingH2DataAdapter extends H2DataAdapter {
        public InstallingH2DataAdapter(String path, String username, String password, IWritableDataQueryProvider dataQueryProvider, String tableName) {
            super(path, username, password, dataQueryProvider, tableName);
        }

        @Override
        protected Connection getConnection(DataSource dataSource) throws SQLException {
            try {
                return super.getConnection(dataSource);
            } catch (Throwable e) {
                if (e instanceof JdbcSQLException) {
                    if ("90013".equals(((JdbcSQLException) e).getSQLState())) {
                        log.error("Could not connect to local H2 database", e);
                        String templateDbPath = System.getProperty("viewserver.templatedbpath");
                        if (templateDbPath != null) {
                            try {
                                Path fromPath = new File(templateDbPath).toPath();
                                Path target = Paths.get(Utils.replaceSystemTokens(getPath() + ".h2.db"));
                                log.info("Will copy packaged database from {} to {}", templateDbPath, target);
                                Files.copy(fromPath, target);
                                return super.getConnection(dataSource);
                            } catch (IOException e1) {
                                throw new RuntimeException(e1);
                            }
                        }
                    }
                }
                throw e;
            }
        }
    }
}
