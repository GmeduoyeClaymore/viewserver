import RxDataSink from 'common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import * as TableNames from 'common/constants/TableNames';

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
    return 'orderItemsDao';
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

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, TableNames.ORDER_ITEM_TABLE_NAME, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.userId != newOptions.userId;
  }

  transformOptions(options){
    const {userId, orderId} = options;
    if (typeof userId === 'undefined'){
      throw new Error('userId should be defined');
    }
    if (typeof orderId === 'undefined'){
      throw new Error('orderId should be defined');
    }
    return {...options, filterExpression: `userId == \"${userId}\" && orderId == "${orderId}"`};
  }
}


