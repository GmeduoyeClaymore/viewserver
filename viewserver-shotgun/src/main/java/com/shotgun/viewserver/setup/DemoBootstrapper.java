package com.shotgun.viewserver.setup;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.server.setup.BootstrapperBase;

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
        dataSources.add(CustomerDataSource.getDataSource());
        dataSources.add(CustomerDeliveryAddressDataSource.getDataSource());
        dataSources.add(DriverDataSource.getDataSource());
        dataSources.add(MerchantDataSource.getDataSource());
        dataSources.add(PaymentCardsDataSource.getDataSource());
        return dataSources;
    }
}
