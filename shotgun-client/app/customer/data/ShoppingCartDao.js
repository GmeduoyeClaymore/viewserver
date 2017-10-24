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
        filterExpression: `CustomerId == "${customerId}"`,
        flags: undefined
      };
    };
  
    constructor(viewserverClient, customerId){
      super();
      this.subscriptionStrategy = new OperatorSubscriptionStrategy(viewserverClient, FieldMappings.SHOPPING_CART_TABLE_NAME);
      this.viewserverClient = viewserverClient;
      this.customerId = customerId;
      this.subscribeToData(this);
      this.rowAddedListeners = [];
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
        cartRowEvent = this.createAddItemtoCartRowEvent(productId, quantity, new Date());
      }

      this.viewserverClient.editTable(FieldMappings.SHOPPING_CART_TABLE_NAME, this, cartRowEvent, clientTablEventPromise);
      const modifiedRows = await clientTablEventPromise;
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return modifiedRows;
    }
  
    createAddItemtoCartRowEvent(productId, quantity, creationDateTime){
      return [{
        type: 0, // ADD
        columnValues: {
          CustomerId: this.customerId,
          ProductId: productId,
          ProductQuantity: quantity,
          ShoppingCartCreationDate: creationDateTime
        }
      }];
    }

    createUpdateCartRowEvent(existingRow, quantity){
      return [{
        type: 1, // UPDATE
        /* rowId: existingRow.rowId,*/
        columnValues: {
          ProductId: existingRow.ProductId,
          ProductQuantity: existingRow.ProductQuantity + quantity
        }
      }];
    }

    getProductRow(productId){
      return this.rows.find(r => r.ProductId === productId);
    }
  
    get cartItems(){
      return this.rows;
    }
}
