package com.shotgun.viewserver.setup.loaders;

import com.shotgun.viewserver.setup.DataSourceRecordLoader;
import com.shotgun.viewserver.setup.ReportRecordLoader;
import io.viewserver.datasource.DataSourceRegistry;
import io.viewserver.datasource.IRecordLoader;
import io.viewserver.datasource.IRecordLoaderCollection;
import io.viewserver.report.ReportRegistry;
import io.viewserver.server.setup.IApplicationGraphDefinitions;

import java.util.HashMap;
import java.util.Map;

public class ApplicationGraphLoaderCollection implements IRecordLoaderCollection {
    private HashMap<String,IRecordLoader> loaders;
    public ApplicationGraphLoaderCollection(IApplicationGraphDefinitions applicationGraphDefinitions) {
        loaders = new HashMap<>();
        loaders.put(ReportRegistry.TABLE_NAME,new ReportRecordLoader(applicationGraphDefinitions));
        loaders.put(DataSourceRegistry.TABLE_NAME,new DataSourceRecordLoader(applicationGraphDefinitions));
    }


    @Override
    public Map<String, IRecordLoader> getDataLoaders() {
        return loaders;
    }

    @Override
    public void close() {

    }
}
