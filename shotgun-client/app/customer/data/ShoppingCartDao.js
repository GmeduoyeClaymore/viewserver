import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/DataSink';
import Logger from '../../viewserver-client/Logger';
import ClientTableEventPromise from '../../common/ClientTableEventPromise';
import CoolRxDataSink from '../../common/CoolRxDataSink';
import OperatorSubscriptionStrategy from '../../common/OperatorSubscriptionStrategy';

export default class ShoppingCartDao extends DataSink(CoolRxDataSink){
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
      this.subscriptionStrategy = new OperatorSubscriptionStrategy(viewserverClient, FieldMappings.ORDER_ITEMS_TABLE_NAME);
      this.viewserverClient = viewserverClient;
      this.customerId = customerId;
      this.subscribeToData(this);
    }

    get shoppingCartSizeObservable(){
      return this.onTotalRowCountObservable;
    }

    subscribeToData(datasink){
      this.subscriptionStrategy.subscribe(datasink, ShoppingCartDao.DEFAULT_OPTIONS(this.customerId));
    }
  
    async addItemtoCart(productId, quantity){
      let cartRowEvent;
      const clientTablEventPromise = new ClientTableEventPromise(this);
      const existingRow = this.getProductRow(productId);

      if (existingRow !== undefined){
        Logger.info(`Updating cart row ${existingRow.rowId}`);
        cartRowEvent = this.createUpdateCartRowEvent(existingRow, quantity);
      } else {
        Logger.info('Adding item to cart');
        cartRowEvent = this.createAddItemtoCartRowEvent(productId, quantity);
      }

      this.viewserverClient.editTable(FieldMappings.ORDER_ITEMS_TABLE_NAME, this, cartRowEvent, clientTablEventPromise);
      const modifiedRows = await clientTablEventPromise;
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return modifiedRows;
    }
  
    createAddItemtoCartRowEvent(productId, quantity){
      return [{
        type: 0, // ADD
        columnValues: {
          customerId: this.customerId,
          productId,
          quantity
        }
      }];
    }

    createUpdateCartRowEvent(existingRow, quantity){
      return [{
        type: 1, // UPDATE
        rowId: existingRow.rowId,
        columnValues: {
          quantity: existingRow.quantity + quantity
        }
      }];
    }

    getProductRow(productId){
      return this.rows.find(r => r.productId === productId);
    }
  
    get cartItems(){
      return this.rows;
    }
}
