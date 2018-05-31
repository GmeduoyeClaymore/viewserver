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
      columnsToSort: [{name: 'lastUsed', direction: 'desc'}],
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
    return 'deliveryAddressDao';
  }

  createDataSink = () => {
    return new RxDataSink(this._name);
  }

  mapDomainEvent(dataSink){
    const deliveryAddresses = dataSink.rows;
    const homeAddress = deliveryAddresses ? deliveryAddresses.find(ad => ad.isDefault) || deliveryAddresses[0] : deliveryAddresses;

    return {
      customer: {
        deliveryAddresses,
        homeAddress
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
}
