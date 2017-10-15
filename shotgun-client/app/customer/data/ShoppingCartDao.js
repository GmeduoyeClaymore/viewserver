import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/DataSink';
import SnapshotCompletePromise from '../../common/SnapshotCompletePromise';
import ExternallyResolvedPromise from '../../common/ExternallyResolvedPromise';
import OperatorSubscriptionStrategy from '../../common/OperatorSubscriptionStrategy';

export default class ShoppingCartDao extends DataSink(SnapshotCompletePromise){
    static DEFAULT_OPTIONS = (customerId) =>  {
      return {
        offset :  0,
        limit :  20,
        columnName  :  undefined,
        columnsToSort :  undefined,
        filterMode :  2,//Filtering
        filterExpression : `CustomerId == "${customerId}"`,
        flags : undefined 
      }
    }
  
    constructor(viewserverClient,customerId){
      super()
      this.subscriptionStrategy = new OperatorSubscriptionStrategy(viewserverClient,FieldMappings.SHOPPING_CART_TABLE_NAME);
      this.viewserverClient = viewserverClient;
      this.customerId = customerId;
      this.subscriptionStrategy.subscribe(this,ShoppingCartDao.DEFAULT_OPTIONS(customerId))
      this.rowAddedListeners = [];
    }
  
    async addItemtoCart(productId,quantity){
      let addItemToCartPromise = new SnapshotCompletePromise();
      let creationDateTime = new Date();
      let addItemRowEvent = this.createAddItemtoCartRowEvent(productId,quantity,creationDateTime);
      this.viewserverClient.editTable(FieldMappings.SHOPPING_CART_TABLE_NAME,this,addItemRowEvent,addItemToCartPromise)
      await addItemToCartPromise;
      let addedItem = await this.waitForItemAddition(addItemRowEvent);
    }
  
    waitForItemAddition(rowAddedEvent){
      let rowAddedPromise = new ExternallyResolvedPromise();
      this.rowAddedListeners[rowAddedEvent] = rowAddedPromise;
      return rowAddedPromise;
    }
  
    onRowAdded(rowId, row){
      super.onRowAdded(rowId, row);
      const listenerersCopy = {...this.rowAddedListeners};
      listenerersCopy.map(
        (evt,listener) => {
          if(this.eventsEqual(evt,row)){
            listener.resolve();
            delete this.rowAddedListeners[evt];
          }
        }
      )
    }
  
    eventsEqual(event,row){
      return event.ShoppingCartCreationDate == row.ShoppingCartCreationDate && event.ProductId === row.ProductId;
    }
  
    createAddItemtoCartRowEvent(productId,quantity,creationDateTime){
      return [{
        type : 0, // ADD
        columnValues : {
          CustomerId : this.customerId,
          ProductId : productId,
          ProductQuantity : quantity,
          ShoppingCartCreationDate : creationDateTime
        }
      }]
    }
  
    get cartItems(){
      return  this.rows;
    }
  }