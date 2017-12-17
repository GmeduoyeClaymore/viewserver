import RxDataSink from 'common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import * as TableNames from 'common/constants/TableNames';
import uuidv4 from 'uuid/v4';
import Logger from 'common/Logger';

const createAddOrderItemRowEvent = (orderItemId, orderId, productId, userId) =>{
  return {
    type: 0, // ADD
    columnValues: {
      orderId,
      orderItemId,
      userId,
      productId,
      quantity: 1
    }
  };
};

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
    /* Sometimes we need to just subscribe to this without an orderId to get the schema :(
    if (typeof orderId === 'undefined'){
      throw new Error('orderId should be defined');
    }*/
    return {...options, filterExpression: `userId == \"${userId}\"` + (orderId !== undefined ? `&& orderId == "${orderId}"` : '')};
  }

  extendDao(dao){
    dao.addOrderItem = async ({orderId, productId, userId}) => {
      Logger.info(`Adding product ${productId} to order ${orderId}`);
      const {subscriptionStrategy} = dao;
      const orderItemId = uuidv4();
      const cartRowEvent = createAddOrderItemRowEvent(orderItemId, orderId, productId, userId);

      const result = dao.rowEventObservable.filter(ev => ev.row.orderItemId === orderItemId).take(1).timeoutWithError(5000, new Error(`Could not detect modification to order item ${orderItemId} in 5 seconds`)).toPromise();
      await subscriptionStrategy.editTable([cartRowEvent]);
      const modifiedRows = await result;
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return modifiedRows.row.orderItemId;
    };
  }
}


