package com.shotgun.viewserver.setup;

import io.viewserver.adapters.common.Record;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.*;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportRecordLoader implements IRecordLoader {

    private IApplicationGraphDefinitions applicationGraphDefinitions;
    private static final Logger log = LoggerFactory.getLogger(ReportRecordLoader.class);

    public ReportRecordLoader(IApplicationGraphDefinitions applicationGraphDefinitions) {
        this.applicationGraphDefinitions = applicationGraphDefinitions;
    }

    @Override
    public SchemaConfig getSchemaConfig() {
        return ReportRegistry.getSchemaConfig();
    }

    @Override
    public Observable<IRecord> getRecords(String query) {
        List<IRecord> result = new ArrayList<>();
        for (ReportDefinition reportDefinition : applicationGraphDefinitions.getReportDefinitions().values()) {
            log.debug("Loading Report - {}", reportDefinition.getId());
            try {
                String json = JacksonSerialiser.getInstance().serialise(reportDefinition, true);

                Map<String, Object> docData = new HashMap<>();
                docData.put("id", reportDefinition.getId());
                docData.put("name", reportDefinition.getName());
                docData.put("dataSource", reportDefinition.getDataSource());
                docData.put("json", json);
                result.add(new Record().initialiseFromRecord(docData));
            } catch (Exception e) {
                log.error(String.format("Could not create report '%s'", reportDefinition.getId()), e);
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
