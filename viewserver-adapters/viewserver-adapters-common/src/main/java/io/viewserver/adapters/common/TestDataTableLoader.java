package io.viewserver.adapters.common;

import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IDataAdapter;
import io.viewserver.datasource.IRecord;
import io.viewserver.schema.Schema;

import java.util.function.Consumer;

/**
 * Created by Gbemiga on 10/12/17.
 */
public class TestDataTableLoader implements IDataAdapter {
    @Override
    public void setDataSource(DataSource dataSource) {

    }

    @Override
    public Schema getDerivedSchema() {
        return null;
    }

    @Override
    public int getRecords(String query, Consumer<IRecord> consumer) {
        return 0;
    }
}
