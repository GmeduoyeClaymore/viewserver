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
        dataSources.add(CustomerDataSource.getDataSource());
        dataSources.add(CustomerDeliveryAddressDataSource.getDataSource());
        dataSources.add(DriverDataSource.getDataSource());
        dataSources.add(MerchantDataSource.getDataSource());
        dataSources.add(PaymentCardsDataSource.getDataSource());
        dataSources.add(DeliveryDataSource.getDataSource());
        dataSources.add(MerchantProductInventoryDataSource.getDataSource());
        dataSources.add(OrderDataSource.getDataSource());
        dataSources.add(OrderFulfillmentDataSource.getDataSource());
        dataSources.add(OrderProductDataSource.getDataSource());
        dataSources.add(PackageTypeDataSource.getDataSource());
        dataSources.add(ProductDataSource.getDataSource());
        dataSources.add(ProductCategoryDataSource.getDataSource());
        dataSources.add(StatusDataSource.getDataSource());
        dataSources.add(OrderItemsDataSource.getDataSource());
        dataSources.add(ShoppingCartDataSource.getDataSource());
        return dataSources;
    }
}
