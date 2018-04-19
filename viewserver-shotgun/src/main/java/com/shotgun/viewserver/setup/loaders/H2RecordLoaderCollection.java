package com.shotgun.viewserver.setup.loaders;

import io.viewserver.adapters.common.sql.SimpleSqlDataQueryProvider;
import io.viewserver.adapters.jdbc.JdbcConnectionFactory;
import io.viewserver.adapters.jdbc.JdbcRecordLoader;
import io.viewserver.datasource.*;
import io.viewserver.report.ReportRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class H2RecordLoaderCollection implements IRecordLoaderCollection {

    private HashMap<String,IRecordLoader> loaders;
    private JdbcConnectionFactory connectionFactory;

    public H2RecordLoaderCollection(JdbcConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        loaders = new HashMap<>();
        register(ReportRegistry.getSchemaConfig(),ReportRegistry.TABLE_NAME, "Reports");
        register(DataSourceRegistry.getSchemaConfig(),DataSourceRegistry.TABLE_NAME, "DataSources");
    }

    private void register(SchemaConfig schema, String operatorName, String sourceTableName) {
        SimpleSqlDataQueryProvider dataQueryProvider = new SimpleSqlDataQueryProvider(sourceTableName);
        loaders.put(operatorName,new JdbcRecordLoader(dataQueryProvider,connectionFactory,schema, new OperatorCreationConfig(CreationStrategy.CREATE,CreationStrategy.CREATE)));
    }

    @Override
    public Map<String, IRecordLoader> getDataLoaders() {
        return loaders;
    }
}


