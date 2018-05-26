package com.shotgun.viewserver.setup;

import io.viewserver.adapters.common.Record;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.*;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSourceRecordLoader implements IRecordLoader {

    private IApplicationGraphDefinitions applicationGraphDefinitions;
    private static final Logger log = LoggerFactory.getLogger(ReportRecordLoader.class);

    public DataSourceRecordLoader(IApplicationGraphDefinitions applicationGraphDefinitions) {
        this.applicationGraphDefinitions = applicationGraphDefinitions;
    }

    @Override
    public SchemaConfig getSchemaConfig() {
        return DataSourceRegistry.getSchemaConfig();
    }

    @Override
    public Observable<IRecord> getRecords(String query) {
        List<IRecord> result = new ArrayList<>();
        for (DataSource dataSource : applicationGraphDefinitions.getDataSources()) {
            log.debug("Loading Datasource -    {}", dataSource.getName());
            try {
                String json = JacksonSerialiser.getInstance().serialise(dataSource, true);

                Map<String, Object> docData = new HashMap<>();
                docData.put("name", dataSource.getName());
                docData.put("json", json);
                result.add(new Record().initialiseFromRecord(docData));
            } catch (Exception e) {
                log.error(String.format("Could not create datasource '%s'", dataSource.getName()), e);
            }
        }
        return Observable.from(result);
    }

    @Override
    public OperatorCreationConfig getCreationConfig() {
        return new OperatorCreationConfig(CreationStrategy.WAIT,CreationStrategy.WAIT);
    }

    @Override
    public void close() {

    }
}
