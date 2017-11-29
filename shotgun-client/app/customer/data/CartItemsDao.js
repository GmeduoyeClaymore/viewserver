import * as TableNames from 'common/constants/TableNames';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import RxDataSink from 'common/dataSinks/RxDataSink';
import uuidv4 from 'uuid/v4';

const createAddOrderItemRowEvent = (productId, quantity, userId) =>{
  return {
    type: 0, // ADD
    columnValues: {
      orderId: null,
      itemId: uuidv4(),
      userId,
      productId,
      quantity
    }
  };
};

const createUpdateCartRowEvent = (tableKey, columnValues) => {
  return {
    type: 1, // UPDATE
    tableKey,
    columnValues
  };
};

const createRemoveCartRowEvent = (tableKey) => {
  return {
    type: 2, // REMOVE
    tableKey,
    columnValues: {}
  };
};

export default class CartItemsDaoContext{
  constructor(client, orderDao, deliveryDao, options = {}) {
    this.client = client;
    this.options = options;
    this.orderDao = orderDao;
    this.deliveryDao = deliveryDao;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 150,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
    return 'cartItemsDao';
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      cart: {
        items: [...dataSink.rows],
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
    const {userId} = options;
    if (typeof userId === 'undefined'){
      throw new Error('userId should be defined');
    }
    return {...options, filterExpression: `userId like \"${userId}\" && orderId == null`};
  }

  extendDao(dao){
    dao.addItemToCart = async ({productId, quantity, userId}) => {
      let cartRowEvent;
      const {dataSink, subscriptionStrategy} = dao;
      const {rows} = dataSink;
      const existingRow = rows.find(r => r.productId === productId);
      let resultKey;
      if (existingRow !== undefined){
        Logger.info(`Updating cart row ${existingRow.itemId}`);
        resultKey = existingRow.itemId;
        cartRowEvent = createUpdateCartRowEvent(existingRow.itemId, {quantity: parseInt(existingRow.quantity, 10) + parseInt(quantity, 10)});
      } else {
        Logger.info('Adding item to cart');
        cartRowEvent = createAddOrderItemRowEvent(productId, quantity, userId);
        resultKey = cartRowEvent.columnValues.itemId;
      }

      await subscriptionStrategy.editTable([cartRowEvent]);
      const result = await dao.rowEventObservable.filter(ev => ev.row.itemId === resultKey).take(1).timeoutWithError(5000, new Error(`Could not detect modification to cart item ${resultKey} in 5 seconds`)).toPromise();
      return result;
    };
    dao.updateItemCartQuantity = async ({productId, quantity}) => {
      let cartRowEvent;
      const {dataSink, subscriptionStrategy} = dao;
      const {rows} = dataSink;
      const existingRow = rows.find(r => r.productId === productId);
      const resultKey = existingRow.itemId;
      if (quantity > 0){
        Logger.info(`Updating cart row ${existingRow.itemId} quantity to ${quantity}`);
        cartRowEvent = createUpdateCartRowEvent(existingRow.itemId, {quantity});
      } else {
        Logger.info('Removing item from cart');
        cartRowEvent = createRemoveCartRowEvent(existingRow.itemId);
      }

      await subscriptionStrategy.editTable([cartRowEvent]);
      const result = await dao.rowEventObservable.filter(ev => ev.row.itemId === resultKey).take(1).timeoutWithError(5000, new Error(`Could not detect modification to cart item ${resultKey} in 5 seconds`)).toPromise();
      return result;
    };

    dao.purchaseCartItems = async ({eta, paymentId, deliveryAddressId, deliveryType}) => {
      const {dataSink, subscriptionStrategy} = dao;
      const {rows} = dataSink;
      if (rows.some(e => e.orderId !== undefined)){
        //defensive coding to make sure we're not updating existing order items by mistake
        Logger.error('Tried to update an item which already has an orderId this shouldnt happen!');
        return;
      }

      const deliveryId = await this.deliveryDao.createDelivery({deliveryAddressId, eta, deliveryType});
      const orderId = await this.orderDao.createOrder({deliveryId, paymentId});
      const rowEvents = rows.map(i => createUpdateCartRowEvent(i.itemId, {orderId}));
      const rowEventPromises = rowEvents.map( ev => dao.rowEventObservable.filter(event => event.row.itemId === ev.tableKey).take(1).timeoutWithError(5000, new Error(`Could not detect modification to cart event \"${ev.tableKey}\" in 5 seconds`)).toPromise());
      await Promise.all([subscriptionStrategy.editTable(rowEvents), ...rowEventPromises]);
      Logger.info('Purchase cart item promise resolved');
      return orderId;
    };
  }
}

