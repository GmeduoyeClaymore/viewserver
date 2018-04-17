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

package io.viewserver.logging.log4j.log4j;

import io.viewserver.authentication.LoggerAuthenticationHandler;
import io.viewserver.client.CommandResult;
import io.viewserver.client.ITableCreationConfig;
import io.viewserver.client.RollingTableCreationConfig;
import io.viewserver.client.RowEvent;
import io.viewserver.operators.table.RollingTable;
import io.viewserver.schema.column.ColumnType;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by nick on 29/09/15.
 */
public class ViewServerManager extends AbstractManager {
    private static final ViewServerManagerFactory factory = new ViewServerManagerFactory();
    private final LoggerClient client;
    private String tableName;
    private List<Column> columns;

    protected ViewServerManager(String name, LoggerClient client, String tableName, List<Column> columns) {
        super(name);
        this.tableName = tableName;
        this.columns = columns;
        this.client = client;
    }

    public static ViewServerManager getManager(String url, String tableName, ColumnConfig[] columnConfigs, int rolloverSize) {
        return AbstractManager.getManager(String.format("%s.%s", url, tableName),
                factory,
                new ViewServerManagerFactoryData(url, tableName, columnConfigs, rolloverSize));
    }

    public void write(LogEvent logEvent) {
        Map<Integer, Object> values = new HashMap<>();
        int count = columns.size();
        for (int i = 0; i < count; i++) {
            Column column = columns.get(i);
            if (column.isEventTimestamp) {
                values.put(i, logEvent.getTimeMillis());
            } else {
                values.put(i, column.layout.toSerializable(logEvent));
            }
        }

        client.editTable(tableName, Collections.singletonList(RowEvent.addRow(values)),null,null,false);
    }

    private static class ViewServerManagerFactory implements ManagerFactory<ViewServerManager, ViewServerManagerFactoryData> {
        @Override
        public ViewServerManager createManager(String name, ViewServerManagerFactoryData data) {
            LoggerClient client;
            try {
                client = new LoggerClient(name, data.url);
            } catch (URISyntaxException e) {
                LOGGER.error("Unable to parse URL " + data.url, e);
                return null;
            }

            // TODO: authentication
            client.authenticate(LoggerAuthenticationHandler.TYPE, "log4j");

            List<ViewServerManager.Column> columns = new ArrayList<>();
            List<io.viewserver.client.Column> clientColumns = new ArrayList<>();
            for (ColumnConfig columnConfig : data.columnConfigs) {
                columns.add(new ViewServerManager.Column(columnConfig.getLayout(), columnConfig.isEventTimestamp()));
                ColumnType columnType;
                if (columnConfig.isEventTimestamp()) {
                    columnType = ColumnType.Long;
                } else {
                    columnType = ColumnType.String;
                }
                clientColumns.add(new io.viewserver.client.Column(columnConfig.getName(), columnType));
            }

            String tableClass = null;
            ITableCreationConfig tableCreationConfig = null;
            if (data.rolloverSize > 0) {
                tableClass = RollingTable.class.getName();
                tableCreationConfig = new RollingTableCreationConfig(data.rolloverSize);
            }

            Futures.addCallback(
                    client.createTable(data.tableName, clientColumns, tableClass, tableCreationConfig),
                    new FutureCallback<CommandResult>() {
                        @Override
                        public void onSuccess(CommandResult result) {

                        }

                        @Override
                        public void onFailure(Throwable t) {
                            LOGGER.error("Could not create table '" + data.tableName + "'", t);
                            // TODO: what should we do now?
                        }
                    });

            return new ViewServerManager(name, client, data.tableName, columns);
        }
    }

    private static class ViewServerManagerFactoryData {
        private final String url;
        private final String tableName;
        private final ColumnConfig[] columnConfigs;
        private final int rolloverSize;

        public ViewServerManagerFactoryData(String url, String tableName, ColumnConfig[] columnConfigs, int rolloverSize) {
            this.url = url;
            this.tableName = tableName;
            this.columnConfigs = columnConfigs;
            this.rolloverSize = rolloverSize;
        }
    }

    private static class Column {
        private final PatternLayout layout;
        private final boolean isEventTimestamp;

        public Column(PatternLayout layout, boolean isEventTimestamp) {
            this.layout = layout;
            this.isEventTimestamp = isEventTimestamp;
        }
    }
}
