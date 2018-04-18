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

import io.viewserver.adapters.common.IDataQueryProvider;
import io.viewserver.adapters.common.IWritableDataQueryProvider;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.IWritableDataAdapter;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.*;
import io.viewserver.util.ViewServerException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by nick on 16/02/2015.
 */
public abstract class JdbcDataAdapterBase implements IWritableDataAdapter {
    private static final Logger log = LoggerFactory.getLogger(JdbcDataAdapterBase.class);
    private DataSource dataSource;
    private final String username;
    private final String password;
    private IWritableDataQueryProvider dataQueryProvider;
    private Schema schema;
    private TableKeyDefinition tableKeyDefinition;
    private final int FETCHSIZE = 10000;

    private final ResultSetRecordWrapper recordWrapper = new ResultSetRecordWrapper();

    protected JdbcDataAdapterBase(String username, String password, IWritableDataQueryProvider dataQueryProvider) {
        this.username = username;
        this.password = password;
        this.dataQueryProvider = dataQueryProvider;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void setDataSource(io.viewserver.datasource.DataSource dataSource) {
        recordWrapper.setDataSource(dataSource);
    }

    @Override
    public Schema getDerivedSchema() {
        Schema derivedSchema = new Schema();
        executeQuery(dataQueryProvider.getSchemaQuery(), (resultSet) -> {
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                int columnType = metaData.getColumnType(i);
                ColumnHolder columnHolder = ColumnHolderUtils.createColumnHolder(metaData.getColumnName(i),
                        getColumnType(columnType));
                ColumnMetadata columnMetadata = ColumnHolderUtils.createColumnMetadata(columnHolder.getType());
                columnMetadata.setDataType(getDataType(columnType));
                columnHolder.setMetadata(columnMetadata);
                derivedSchema.addColumn(columnHolder);
            }
        });
        return derivedSchema;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @JsonIgnore
    @Override
    public TableKeyDefinition getDerivedTableKeyDefinition() {
        return null;
    }

    @Override
    public void setTableKeyDefinition(TableKeyDefinition tableKeyDefinition) {
        this.tableKeyDefinition = tableKeyDefinition;
    }

    @Override
    public int getRecords(String query, Consumer<IRecord> consumer) {
        final int[] recordCount = new int[1];

        executeQuery(query, (resultSet) -> {
            recordWrapper.setResultSet(resultSet);
            while (resultSet.next()) {
                consumer.accept(recordWrapper);
                recordCount[0] = resultSet.getRow();
            }
            recordWrapper.setResultSet(null);
        });

        return recordCount[0];
    }

    @Override
    public void insertRecord(ITableRow tableRow) {

        try (PreparedStatement statement = this.getPreparedStatement(((IWritableDataQueryProvider) dataQueryProvider).getInsertQuery())) {
            int paramIndex = 1;
            List<ColumnHolder> columnHolders = schema.getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
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

        try (PreparedStatement statement = this.getPreparedStatement(((IWritableDataQueryProvider) dataQueryProvider).getUpdateQuery(tableKeyDefinition))) {
            int paramIndex = 1;
            List<ColumnHolder> columnHolders = schema.getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null || tableKeyDefinition.getKeys().contains(columnHolder.getName())) {
                    continue;
                }
                setPlaceholderValue(statement, paramIndex++, tableRow, columnHolder);
            }
            for (String key : tableKeyDefinition.getKeys()) {
                ColumnHolder columnHolder = schema.getColumnHolder(key);
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

        try (PreparedStatement statement = this.getPreparedStatement(((IWritableDataQueryProvider) dataQueryProvider).getDeleteQuery(tableKeyDefinition))) {
            int paramIndex = 1;
            for (String key : tableKeyDefinition.getKeys()) {
                ColumnHolder columnHolder = schema.getColumnHolder(key);
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

        try (PreparedStatement statement =  this.getPreparedStatement(((IWritableDataQueryProvider) dataQueryProvider).getDeleteQuery())){
            statement.execute();
        } catch (SQLException e) {
            log.error("Could not clear data", e);
        }
    }

    private void setPlaceholderValue(PreparedStatement statement, int paramIndex, ITableRow tableRow, ColumnHolder columnHolder) throws SQLException {
        ColumnMetadata metadata = columnHolder.getMetadata();
        switch (metadata.getDataType()) {
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

    private ColumnType getColumnType(int columnType) {
        switch (columnType) {
            case Types.BOOLEAN: {
                return ColumnType.Bool;
            }
            case Types.TINYINT: {
                return ColumnType.Byte;
            }
            case Types.SMALLINT: {
                return ColumnType.Short;
            }
            case Types.INTEGER: {
                return ColumnType.Int;
            }
            case Types.BIGINT:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP: {
                return ColumnType.Long;
            }
            case Types.REAL: {
                return ColumnType.Float;
            }
            case Types.FLOAT:
            case Types.DOUBLE: {
                return ColumnType.Double;
            }
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB: {
                return ColumnType.String;
            }
            default: {
                throw new IllegalArgumentException("Cannot handle column type " + columnType);
            }
        }
    }

    private io.viewserver.datasource.ColumnType getDataType(int columnType) {
        switch (columnType) {
            case Types.BOOLEAN: {
                return io.viewserver.datasource.ColumnType.Bool;
            }
            case Types.TINYINT: {
                return io.viewserver.datasource.ColumnType.Byte;
            }
            case Types.SMALLINT: {
                return io.viewserver.datasource.ColumnType.Short;
            }
            case Types.INTEGER: {
                return io.viewserver.datasource.ColumnType.Int;
            }
            case Types.BIGINT: {
                return io.viewserver.datasource.ColumnType.Long;
            }
            case Types.DATE: {
                return io.viewserver.datasource.ColumnType.Date;
            }
            case Types.TIME:
            case Types.TIMESTAMP: {
                return io.viewserver.datasource.ColumnType.DateTime;
            }
            case Types.REAL: {
                return io.viewserver.datasource.ColumnType.Float;
            }
            case Types.FLOAT:
            case Types.DOUBLE: {
                return io.viewserver.datasource.ColumnType.Double;
            }
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB: {
                return io.viewserver.datasource.ColumnType.String;
            }
            default: {
                throw new IllegalArgumentException("Cannot handle column type " + columnType);
            }
        }
    }

    protected void executeQuery(String query, IResultSetHandler resultSetHandler) {
        try (PreparedStatement statement = getPreparedStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.setFetchSize(FETCHSIZE);
                resultSetHandler.handle(resultSet);
            }
            statement.close();
        } catch (SQLException e) {
            log.error("Failed to execute query " + query, e);
        }
    }

    protected Connection getConnection(DataSource dataSource) throws SQLException {
        return dataSource.getConnection(username, password);
    }

    protected PreparedStatement getPreparedStatement(String query) throws SQLException {
        DataSource dataSource = getDataSource();
        Connection connection = getConnection(dataSource);
        if (query.startsWith("{call")) {
            return connection.prepareCall(query);
        }

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setFetchSize(FETCHSIZE);

        return preparedStatement;
    }

    private DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
        return dataSource;
    }

    protected abstract DataSource createDataSource();



    protected interface IResultSetHandler {
        void handle(ResultSet resultSet) throws SQLException;
    }
}
