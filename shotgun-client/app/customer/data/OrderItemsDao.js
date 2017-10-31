import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/dataSinks/DataSink';
import Logger from '../../viewserver-client/Logger';
import TableEditPromise from '../../common/promises/TableEditPromise';
import CoolRxDataSink from '../../common/dataSinks/CoolRxDataSink';
import OperatorSubscriptionStrategy from '../../common/subscriptionStrategies/OperatorSubscriptionStrategy';
import uuidv4 from 'uuid/v4';

export default class OrderItems extends DataSink(CoolRxDataSink){
    static DEFAULT_OPTIONS = (customerId) =>  {
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
  
    constructor(viewserverClient, customerId){
      super();
      this.subscriptionStrategy = new OperatorSubscriptionStrategy(viewserverClient, FieldMappings.ORDER_ITEM_TABLE_NAME);
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

      const tableEditPromise = new TableEditPromise();
      this.viewserverClient.editTable(FieldMappings.ORDER_ITEM_TABLE_NAME, this, [cartRowEvent], tableEditPromise);
      const modifiedRows = await tableEditPromise;
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return modifiedRows;
    }

    async purchaseCartItems(orderId){
      const rowEvents = this.rows.map(i => this.createUpdateCartRowEvent(i.itemId, {orderId}));
      const tableEditPromise = new TableEditPromise();
      this.viewserverClient.editTable(FieldMappings.ORDER_ITEM_TABLE_NAME, this, rowEvents, tableEditPromise);
      await tableEditPromise;
      Logger.info('purchase cart item promise resolved');
    }

    createAddOrderItemRowEvent(productId, quantity){
      return {
        type: 0, // ADD
        columnValues: {
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
