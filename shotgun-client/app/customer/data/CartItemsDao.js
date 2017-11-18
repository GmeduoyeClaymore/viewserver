import * as FieldMappings from 'common/constants/TableNames';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import RxDataSink from 'common/dataSinks/RxDataSink';

const createAddOrderItemRowEvent = (productId, quantity) =>{
  return {
    type: 0, // ADD
    columnValues: {
      orderId: null,
      itemId: uuidv4(),
      customerId: this.customerId,
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

export default class CartItemsDaoContext{
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
      return 'cartItems';
  }

  createDataSink(){
      return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      cart: {
        items: dataSink.rows
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
    const {customerId} = options;
    if (typeof customerId === 'undefined'){
      throw new Error('customerId should be defined');
    }
    return {...options, filterExpression: `customerId == "${customerId}" && orderId == null`};
  }

  extendDao(dao){
    dao.addItemToCart = async ({productId, quantity}) => {
      let cartRowEvent;
      const {dataSink, subscriptionStrategy} = dao;
      const {rows} = dataSink;
      const existingRow = rows.find(r => r.productId === productId);

      if (existingRow !== undefined){
        Logger.info(`Updating cart row ${existingRow.itemId}`);
        cartRowEvent = createUpdateCartRowEvent(existingRow.itemId, {quantity: parseInt(existingRow.quantity, 10) + parseInt(quantity, 10)});
      } else {
        Logger.info('Adding item to cart');
        cartRowEvent = createAddOrderItemRowEvent(productId, quantity);
      }

      const modifiedRows = await subscriptionStrategy.editTable(this, [cartRowEvent]);
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      const result = await dao.rowEventObservable.filter(row => row.itemId == cartRowEvent.columnValues.itemId).timeout(5000, `Could not modification to cart item ${cartRowEvent.columnValues.itemId} in 5 seconds`).toPromise();
      return result;
    };
    dao.purchaseCartItems = async (orderId) => {
      const {dataSink, subscriptionStrategy} = dao;
      const {rows} = dataSink;
      if (rows.some(e => e.orderId !== undefined)){
        //defensive coding to make sure we're not updating existing order items by mistake
        Logger.error('Tried to update an item which already has an orderId this shouldnt happen!');
        return;
      }
      const rowEvents = rows.map(i => createUpdateCartRowEvent(i.itemId, {orderId}));
      await subscriptionStrategy.editTable(dataSink, rowEvents);
      const result = await Promise.all(rowEvents.map( ev => dao.rowEventObservable.filter(row => row.itemId == ev.columnValues.itemId).timeout(5000, `Could not modification to cart event ${ev.columnValues.itemId} in 5 seconds`).toPromise()));
      Logger.info('Purchase cart item promise resolved');
      return result;
    };
  }
}

