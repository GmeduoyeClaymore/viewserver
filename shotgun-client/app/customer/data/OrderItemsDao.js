import RxDataSink from 'common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import * as FieldMappings from 'common/constants/TableNames';

export default class OrderItemsDaoContext{
  constructor(client, options = {}) {
    this.client = client;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
      return 'orderItems';
  }

  createDataSink(){
      return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      customer: {
        orderDetail: {
          items: dataSink.rows
        }
      }
    };
  }

  createSubscriptionStrategy(){
    return new DataSourceSubscriptionStrategy(this.client, FieldMappings.ORDER_ITEM_TABLE_NAME);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.customerId != newOptions.customerId;
  }

  transformOptions(options){
    const {customerId, orderId} = options;
    if (typeof customerId === 'undefined'){
      throw new Error('customerId should be defined');
    }
    if (typeof orderId === 'undefined'){
      throw new Error('orderId should be defined');
    }
    return {...options, filterExpression: `customerId == "${customerId}" && orderId == "${orderId}"`};
  }
}


