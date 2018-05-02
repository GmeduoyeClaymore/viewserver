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

package io.viewserver.adapters.jdbc;

import io.viewserver.adapters.common.IWritableDataQueryProvider;
import io.viewserver.adapters.common.Record;
import io.viewserver.datasource.*;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.column.IRowFlags;
import io.viewserver.util.ViewServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class JdbcRecordLoader implements IWritableRecordLoader {
    private static final Logger log = LoggerFactory.getLogger(JdbcRecordLoader.class);
    private final ResultSetRecordWrapper resultSetRecordWrapper;
    private OperatorCreationConfig creationConfig;
    private IWritableDataQueryProvider dataQueryProvider;
    private JdbcConnectionFactory connectionFactory;
    private SchemaConfig config;
    private TableKeyDefinition tableKeyDefinition;
    private final int FETCHSIZE = 10000;


    public JdbcRecordLoader(IWritableDataQueryProvider dataQueryProvider, JdbcConnectionFactory connectionFactory, SchemaConfig config, OperatorCreationConfig creationConfig) {
        this.dataQueryProvider = dataQueryProvider;
        this.connectionFactory = connectionFactory;
        this.config = config;
        this.resultSetRecordWrapper  = new ResultSetRecordWrapper(config);
        this.creationConfig = creationConfig;
    }

    public SchemaConfig getDerivedSchema() {
        SchemaConfig derivedSchema = new SchemaConfig();
        executeQuery(dataQueryProvider.getSchemaQuery(), (resultSet) -> {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<Column> result = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                int columnType = metaData.getColumnType(i);
                result.add(new Column(metaData.getColumnName(i),getColumnType(columnType)));
            }
        });
        return derivedSchema;
    }

    @Override
    public OperatorCreationConfig getCreationConfig() {
        return creationConfig;
    }

    @Override
    public void close() {

    }

    @Override
    public TableKeyDefinition getDerivedTableKeyDefinition() {
        return null;
    }

    @Override
    public void setTableKeyDefinition(TableKeyDefinition tableKeyDefinition) {
        this.tableKeyDefinition = tableKeyDefinition;
    }

    @Override
    public SchemaConfig getSchemaConfig() {
        return config;
    }

    @Override
    public Observable<IRecord> getRecords(String query) {
        final int[] recordCount = new int[1];
        return  rx.Observable.create(subscriber -> {
            try{
                executeQuery(dataQueryProvider.getSnapshotQuery(), (resultSet) -> {
                    while (resultSet.next()) {
                        subscriber.onNext(getRecord(resultSet));
                        recordCount[0] = resultSet.getRow();
                    }
                });
                subscriber.onCompleted();
            }catch (Exception ex){
                subscriber.onError(ex);
            }}, Emitter.BackpressureMode.BUFFER);
    }

    private IRecord getRecord(ResultSet resultSet){
        Record result = new Record();
        resultSetRecordWrapper.setResultSet(resultSet);
        result.initialiseFromRecord(resultSetRecordWrapper);
        return result;
    }

    @Override
    public void insertRecord(ITableRow tableRow) {

        try (PreparedStatement statement = this.getPreparedStatement(dataQueryProvider.getInsertQuery())) {
            int paramIndex = 1;
            List<Column> columnHolders = config.getColumns();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                Column columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }
                setPlaceholderValue(statement, paramIndex++, tableRow, columnHolder);
            }
            statement.execute();
        } catch (SQLException e) {
            log.error("Could not add row", e);
        }
    }

    @Override
    public void updateRecord(ITableRow tableRow, IRowFlags rowFlags) {

        try (PreparedStatement statement = this.getPreparedStatement(dataQueryProvider.getUpdateQuery(tableKeyDefinition))) {
            int paramIndex = 1;

            for(Column col : this.config.getColumns()){
                if (tableKeyDefinition.getKeys().contains(col.getName())) {
                    continue;
                }
                setPlaceholderValue(statement, paramIndex++, tableRow, col);
            }

            for (String key : tableKeyDefinition.getKeys()) {
                Column columnHolder = this.config.getColumn(key);
                setPlaceholderValue(statement, paramIndex++, tableRow, columnHolder);
            }
            statement.execute();
        } catch (SQLException e) {
            log.error("Could not update row", e);
        }
    }

    @Override
    public void deleteRecord(ITableRow tableRow) {
        if (!(dataQueryProvider instanceof IWritableDataQueryProvider)) {
            throw new ViewServerException("Delete can only be called on a IWritableDataQueryProvider");
        }

        try (PreparedStatement statement = this.getPreparedStatement(dataQueryProvider.getDeleteQuery(tableKeyDefinition))) {
            int paramIndex = 1;
            for (String key : tableKeyDefinition.getKeys()) {
                Column columnHolder = this.config.getColumn(key);
                setPlaceholderValue(statement, paramIndex++, tableRow, columnHolder);
            }
            statement.execute();
        } catch (SQLException e) {
            log.error("Could not remove row", e);
        }
    }

    @Override
    public void clearData() {
        if (!(dataQueryProvider instanceof IWritableDataQueryProvider)) {
            throw new ViewServerException("Clear can only be called on a IWritableDataQueryProvider");
        }

        try (PreparedStatement statement =  this.getPreparedStatement(dataQueryProvider.getDeleteQuery())){
            statement.execute();
        } catch (SQLException e) {
            log.error("Could not clear data", e);
        }
    }

    private void setPlaceholderValue(PreparedStatement statement, int paramIndex, ITableRow tableRow, Column columnHolder) throws SQLException {
        switch (columnHolder.getType()) {
            case Bool: {
                statement.setBoolean(paramIndex, tableRow.getBool(columnHolder.getName()));
                break;
            }
            case Byte: {
                statement.setByte(paramIndex, tableRow.getByte(columnHolder.getName()));
                break;
            }
            case Short: {
                statement.setShort(paramIndex, tableRow.getShort(columnHolder.getName()));
                break;
            }
            case Int: {
                statement.setInt(paramIndex, tableRow.getInt(columnHolder.getName()));
                break;
            }
            case Long: {
                statement.setLong(paramIndex, tableRow.getLong(columnHolder.getName()));
                break;
            }
            case Float: {
                statement.setFloat(paramIndex, tableRow.getFloat(columnHolder.getName()));
                break;
            }
            case Double: {
                statement.setDouble(paramIndex, tableRow.getDouble(columnHolder.getName()));
                break;
            }
            case String: {
                statement.setString(paramIndex, tableRow.getString(columnHolder.getName()));
                break;
            }
            case DateTime: {
                statement.setDate(paramIndex, new java.sql.Date(tableRow.getLong(columnHolder.getName())));
                break;
            }
            case Date: {
                statement.setTimestamp(paramIndex, new Timestamp(tableRow.getLong(columnHolder.getName())));
                break;
            }
        }
    }

    private ContentType getColumnType(int columnType) {
        switch (columnType) {
            case Types.BOOLEAN: {
                return ContentType.Bool;
            }
            case Types.TINYINT: {
                return ContentType.Byte;
            }
            case Types.SMALLINT: {
                return ContentType.Short;
            }
            case Types.INTEGER: {
                return ContentType.Int;
            }
            case Types.BIGINT:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP: {
                return ContentType.Long;
            }
            case Types.REAL: {
                return ContentType.Float;
            }
            case Types.FLOAT:
            case Types.DOUBLE: {
                return ContentType.Double;
            }
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB: {
                return ContentType.String;
            }
            default: {
                throw new IllegalArgumentException("Cannot handle column type " + columnType);
            }
        }
    }

    private ContentType getDataType(int columnType) {
        switch (columnType) {
            case Types.BOOLEAN: {
                return ContentType.Bool;
            }
            case Types.TINYINT: {
                return ContentType.Byte;
            }
            case Types.SMALLINT: {
                return ContentType.Short;
            }
            case Types.INTEGER: {
                return ContentType.Int;
            }
            case Types.BIGINT: {
                return ContentType.Long;
            }
            case Types.DATE: {
                return ContentType.Date;
            }
            case Types.TIME:
            case Types.TIMESTAMP: {
                return ContentType.DateTime;
            }
            case Types.REAL: {
                return ContentType.Float;
            }
            case Types.FLOAT:
            case Types.DOUBLE: {
                return ContentType.Double;
            }
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB: {
                return ContentType.String;
            }
            default: {
                throw new IllegalArgumentException("Cannot handle column type " + columnType);
            }
        }
    }

    protected void executeQuery(String query, IResultSetHandler resultSetHandler) {
        try(PreparedStatement statement = getPreparedStatement(query)){
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.setFetchSize(FETCHSIZE);
                resultSetHandler.handle(resultSet);
            }
            statement.close();
        } catch (SQLException e) {
            log.error("Failed to execute query " + query, e);
        }
    }

    protected PreparedStatement getPreparedStatement(String query) throws SQLException {
        Connection connection = connectionFactory.getConnection();
        if (query.startsWith("{call")) {
            return connection.prepareCall(query);
        }

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setFetchSize(FETCHSIZE);

        return preparedStatement;
    }


    protected interface IResultSetHandler {
        void handle(ResultSet resultSet) throws SQLException;
    }
}
