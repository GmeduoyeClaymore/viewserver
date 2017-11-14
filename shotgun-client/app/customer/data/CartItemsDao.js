import * as FieldMappings from '../../common/constants/TableNames';
import * as constants from '../../redux/ActionConstants';
import DispatchingDataSink from '../../common/dataSinks/DispatchingDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import uuidv4 from 'uuid/v4';
import Logger from '../../viewserver-client/Logger';

export default class CartItemsDao extends DispatchingDataSink {
  static DEFAULT_OPTIONS = (customerId) => {
    return {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      filterExpression: `customerId == "${customerId}" && orderId == null`,
      flags: undefined
    };
  };

  constructor(viewserverClient, customerId, dispatch) {
    super();
    this.dispatch = dispatch;
    this.customerId = customerId;
    this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.ORDER_ITEM_TABLE_NAME);
    this.subscriptionStrategy.subscribe(this, CartItemsDao.DEFAULT_OPTIONS(customerId));
  }

  dispatchUpdate(){
    this.dispatch({type: constants.UPDATE_CART, cart: {items: this.rows}});
  }

  async addItemToCart(productId, quantity){
    let cartRowEvent;

    const existingRow = this.getProductRow(productId);

    if (existingRow !== undefined){
      Logger.info(`Updating cart row ${existingRow.itemId}`);
      cartRowEvent = this.createUpdateCartRowEvent(existingRow.itemId, {quantity: parseInt(existingRow.quantity, 10) + parseInt(quantity, 10)});
    } else {
      Logger.info('Adding item to cart');
      cartRowEvent = this.createAddOrderItemRowEvent(productId, quantity);
    }

    const modifiedRows = await this.subscriptionStrategy.editTable(this, [cartRowEvent]);
    Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
    return modifiedRows;
  }

  async purchaseCartItems(orderId){
    if (this.rows.some(e => e.orderId !== undefined)){
      //defensive coding to make sure we're not updating existing order items by mistake
      Logger.error('Tried to update an item which already has an orderId this shouldnt happen!');
      return;
    }

    const rowEvents = this.rows.map(i => this.createUpdateCartRowEvent(i.itemId, {orderId}));
    await this.subscriptionStrategy.editTable(this, rowEvents);
    Logger.info('Purchase cart item promise resolved');
  }

  createAddOrderItemRowEvent(productId, quantity){
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
  }

  createUpdateCartRowEvent(tableKey, columnValues){
    return {
      type: 1, // UPDATE
      tableKey,
      columnValues
    };
  }

  getProductRow(productId){
    return this.rows.find(r => r.productId === productId);
  }
}
