package com.shotgun.viewserver.setup;

import com.shotgun.viewserver.setup.datasource.CsvDataSource;
import com.shotgun.viewserver.setup.datasource.FxRatesDataSource;
import io.viewserver.server.setup.BootstrapperBase;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.*;
import java.util.Collection;

/**
 * Created by nick on 10/09/15.
 */
public class DemoBootstrapper extends BootstrapperBase {
    @Override
    protected Collection<io.viewserver.datasource.DataSource> getDataSources() {
        Collection<io.viewserver.datasource.DataSource> dataSources = super.getDataSources();
        dataSources.add(FxRatesDataSource.getDataSource());
        dataSources.add(CsvDataSource.getDataSource());
        return dataSources;
    }
}
