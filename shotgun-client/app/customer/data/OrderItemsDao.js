import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/DataSink';
import Logger from '../../viewserver-client/Logger';
import ClientTableEventPromise from '../../common/ClientTableEventPromise';
import CoolRxDataSink from '../../common/CoolRxDataSink';
import OperatorSubscriptionStrategy from '../../common/OperatorSubscriptionStrategy';

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
      this.subscribeToData(this);
    }

    get shoppingCartSizeObservable(){
      return this.onTotalRowCountObservable;
    }

    subscribeToData(datasink){
      this.subscriptionStrategy.subscribe(datasink, OrderItems.DEFAULT_OPTIONS(this.customerId));
    }
  
    async addItemToCart(productId, quantity){
      let cartRowEvent;
      
      const existingRow = this.getProductRow(productId);

      if (existingRow !== undefined){
        Logger.info(`Updating cart row ${existingRow.rowId}`);
        cartRowEvent = this.createUpdateCartRowEvent(existingRow.rowId, {quantity: parseInt(existingRow.quantity, 10) + parseInt(quantity, 10)});
      } else {
        Logger.info('Adding item to cart');
        cartRowEvent = this.createAddOrderItemRowEvent(productId, quantity);
      }

      const clientTablEventPromise = new ClientTableEventPromise(this, [cartRowEvent]);
      this.viewserverClient.editTable(FieldMappings.ORDER_ITEM_TABLE_NAME, this, [cartRowEvent], clientTablEventPromise);
      const modifiedRows = await clientTablEventPromise;
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return modifiedRows;
    }

    async purchaseCartItems(orderId){
      const rowEvents = this.rows.map(i => this.createUpdateCartRowEvent(i.rowId, {orderId}));
      const clientTableEventPromise = new ClientTableEventPromise(this, rowEvents);
      this.viewserverClient.editTable(FieldMappings.ORDER_ITEM_TABLE_NAME, this, rowEvents, clientTableEventPromise);
      await clientTableEventPromise;
      Logger.info('purchase cart item promise resolved');
    }

    createAddOrderItemRowEvent(productId, quantity){
      return {
        type: 0, // ADD
        columnValues: {
          customerId: this.customerId,
          productId,
          quantity
        }
      };
    }

    createUpdateCartRowEvent(rowId, columnValues){
      return {
        type: 1, // UPDATE
        rowId,
        columnValues
      };
    }

    getProductRow(productId){
      return this.rows.find(r => r.productId === productId);
    }
}
