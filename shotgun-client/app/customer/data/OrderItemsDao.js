import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/dataSinks/DataSink';
import Logger from '../../viewserver-client/Logger';
import CoolRxDataSink from '../../common/dataSinks/CoolRxDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import uuidv4 from 'uuid/v4';

export default class OrderItems extends DataSink(CoolRxDataSink){
    static DEFAULT_OPTIONS = (customerId) =>  {
      return {
        offset: 0,
        limit: 20,
        columnName: undefined,
        columnsToSort: undefined,
        filterMode: 2, //Filtering
        filterExpression: `customerId == "${customerId}" && orderId == null`, //TODO - this is quite dangerous is we remove the filter all a users orders could be updated.
        flags: undefined
      };
    };
  
    constructor(viewserverClient, customerId){
      super();
      this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.ORDER_ITEM_TABLE_NAME);
      this.viewserverClient = viewserverClient;
      this.customerId = customerId;
      this.subscriptionStrategy.subscribe(this, OrderItems.DEFAULT_OPTIONS(this.customerId));
    }

    get shoppingCartSizeObservable(){
      return this.onTotalRowCountObservable;
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
