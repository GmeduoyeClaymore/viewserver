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

import io.viewserver.adapters.common.BaseRecordWrapper;
import io.viewserver.core.NullableBool;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.util.ViewServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Paul on 30/09/2015.
 */
public class ResultSetRecordWrapper extends BaseRecordWrapper {
    private ResultSet resultSet;
    private Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private static final Logger log = LoggerFactory.getLogger(ResultSetRecordWrapper.class);

    public ResultSetRecordWrapper(SchemaConfig config) {
        super(config);
    }


    public ResultSetRecordWrapper(ResultSet resultSet, SchemaConfig config) {
        super(config);
        this.resultSet = resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    private String[] getResultSetColumnNames(){
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }
            return columnNames;
        } catch (SQLException e) {
            throw new ViewServerException(e);
        }
    }



    @Override
    public boolean getBool(String columnName) {
        try {
            return resultSet.getBoolean(columnName);
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return false;
        }
    }

    @Override
    public NullableBool getNullableBool(String columnName) {
        try {
            boolean value = resultSet.getBoolean(getDataSourceColumnName(columnName));
            if (resultSet.wasNull()) {
                return NullableBool.Null;
            }
            return NullableBool.fromBoolean(value);
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return null;
        }
    }

    @Override
    public byte getByte(String columnName) {
        try {
            return resultSet.getByte(getDataSourceColumnName(columnName));
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return -1;
        }
    }

    @Override
    public short getShort(String columnName) {
        try {
            return resultSet.getShort(getDataSourceColumnName(columnName));
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return -1;
        }
    }

    @Override
    public int getInt(String columnName) {
        try {
            return resultSet.getInt(getDataSourceColumnName(columnName));
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return -1;
        }
    }

    @Override
    public long getLong(String columnName) {
        try {
            return resultSet.getLong(getDataSourceColumnName(columnName));
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return -1;
        }
    }

    @Override
    public float getFloat(String columnName) {
        try {
            return resultSet.getFloat(getDataSourceColumnName(columnName));
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return -1;
        }
    }

    @Override
    public double getDouble(String columnName) {
        try {
            return resultSet.getDouble(getDataSourceColumnName(columnName));
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return -1;
        }
    }

    @Override
    public String getString(String columnName) {
        try {
            String value = resultSet.getString(getDataSourceColumnName(columnName));
            return this.replaceNullValues(columnName, value, String.class);
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return null;
        }
    }



    @Override
    public Date getDate(String columnName) {
        try {
            return resultSet.getDate(getDataSourceColumnName(columnName), cal);
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return null;
        }
    }

    @Override
    public Date getDateTime(String columnName) {
        try {
            return resultSet.getTimestamp(getDataSourceColumnName(columnName), cal);
        } catch (SQLException e) {
            log.trace(String.format("Issue getting column %s",columnName),e);
            return null;
        }
    }

    @Override
    public Object getValue(String columnName) {
        Column column = this.config.getColumn(columnName);
        switch (column.getType()) {
            case Bool: {
                return getBool(columnName);
            }
            case NullableBool: {
                return getNullableBool(columnName);
            }
            case Byte: {
                return getByte(columnName);
            }
            case Short: {
                return getShort(columnName);
            }
            case Int: {
                return getInt(columnName);
            }
            case Long: {
                return getLong(columnName);
            }
            case Float: {
                return getFloat(columnName);
            }
            case Double: {
                return getDouble(columnName);
            }
            case String: {
                return getString(columnName);
            }
            case Date: {
                return getDate(columnName);
            }
            case DateTime: {
                return getDateTime(columnName);
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unsupported column type %s in record", column.getType()));
            }
        }

    }

    private String getDataSourceColumnName(String columnName) {
        return  columnName;
    }


    @Override
    public boolean hasValue(String columnName) {
        return true;
    }
}