import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/DataSink';
import Logger from '../../viewserver-client/Logger';
import ClientTableEventPromise from '../../common/ClientTableEventPromise';
import CoolRxDataSink from '../../common/CoolRxDataSink';
import ExternallyResolvedPromise from '../../common/ExternallyResolvedPromise';
import OperatorSubscriptionStrategy from '../../common/OperatorSubscriptionStrategy';
import Rx from 'rx-lite';


export default class ShoppingCartDao extends DataSink(CoolRxDataSink){
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
      this.subscribeToData(this);
      this.rowAddedListeners = [];
    }

    get shoppingCartSizeObservable(){
      return  this.onTotalRowCountObservable;
    }

    subscribeToData(datasink){
      this.subscriptionStrategy.subscribe(datasink,ShoppingCartDao.DEFAULT_OPTIONS(this.customerId))
    }
  
    async addItemtoCart(productId,quantity){
      let clientTablEventPromise = new ClientTableEventPromise(this);
      Logger.info(`Adding item to cart`)
      let creationDateTime = new Date();
      let addItemRowEvent = this.createAddItemtoCartRowEvent(productId,quantity,creationDateTime);
      this.viewserverClient.editTable(FieldMappings.SHOPPING_CART_TABLE_NAME,this,addItemRowEvent,clientTablEventPromise)
      let modifiedRows = await clientTablEventPromise;
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`)
      return modifiedRows;
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