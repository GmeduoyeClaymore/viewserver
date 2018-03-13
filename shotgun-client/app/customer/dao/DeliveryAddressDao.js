import RxDataSink from 'common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import * as TableNames from 'common/constants/TableNames';

export default class DeliveryAddressDao{
  constructor(client, options = {}) {
    this.client = client;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 100,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
    return 'deliveryAddressDao';
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(dataSink){
    return {
      customer: {
        deliveryAddresses: dataSink.rows
      }
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, TableNames.DELIVERY_ADDRESS_TABLE_NAME, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.userId != newOptions.userId;
  }

  transformOptions(options){
    return {...options, filterExpression: 'userId == \"@userId\"'};
  }

  extendDao(dao){
    dao.addOrUpdateDeliveryAddress = async ({deliveryAddress}) => {
      Logger.info('add or update delivery address');

      //TODO - not very keen on this logic being here
      const defaultAddress = dao.dataSink.rows.find(ad => ad.isDefault);
      //if we already have a default address then set it to not default
      if (defaultAddress !== undefined && deliveryAddress.deliveryAddressId !== defaultAddress.deliveryAddressId){
        await this.client.invokeJSONCommand('deliveryAddressController', 'addOrUpdateDeliveryAddress', {deliveryAddress: {...defaultAddress, isDefault: false}});
      }

      const deliveryAddressId = await this.client.invokeJSONCommand('deliveryAddressController', 'addOrUpdateDeliveryAddress', {deliveryAddress});
      Logger.info(`Delivery address ${deliveryAddressId} added or updated`);
      return deliveryAddressId;
    };
  }
}
