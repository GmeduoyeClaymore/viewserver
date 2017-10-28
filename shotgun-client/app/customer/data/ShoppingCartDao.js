import * as FieldMappings from './FieldMappings';
import SimpleDataSink from '../../common/SimpleDataSink';
import Logger from '../../viewserver-client/Logger';
import ClientTableEventPromise from '../../common/ClientTableEventPromise';
import ReportSubscriptionStrategy from '../../common/ReportSubscriptionStrategy';
import OperatorSubscriptionStrategy from '../../common/OperatorSubscriptionStrategy';

export default class ShoppingCartDao{
    static DEFAULT_OPTIONS = {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      filterExpression: undefined, //`customerId == "${customerId}" && orderId == null`,
      flags: undefined
    };
  
    constructor(viewserverClient, customerId){
      this.customerId = customerId;
      this.orderItemsSubscriptionStrategy = new ReportSubscriptionStrategy(viewserverClient, this.createReportContext('orderItemByCustomerId'));
      this.orderItemsTotalSubscriptionStrategy = new ReportSubscriptionStrategy(viewserverClient, this.createReportContext('orderItemTotalsByCustomerId'));
      this.orderItemTableSubscriptionStrategy = new OperatorSubscriptionStrategy(viewserverClient, FieldMappings.ORDER_ITEM_TABLE_NAME);
      this.viewserverClient = viewserverClient;
      this.orderItemTotalsDataSink  = new SimpleDataSink();
      this.orderItemsDataSink  = new SimpleDataSink();
      this.orderItemTableDatasink  = new SimpleDataSink();

      this.shoppingCartItemsQuantity = this.orderItemTotalsDataSink.onRowAddedOrUpdatedObservable.select(row => row.sumQuantity);
      
      this.orderItemsTotalSubscriptionStrategy.subscribe(this.orderItemTotalsDataSink, {...ShoppingCartDao.DEFAULT_OPTIONS, limit: 1});
      this.orderItemsSubscriptionStrategy.subscribe(this.orderItemsDataSink, {...ShoppingCartDao.DEFAULT_OPTIONS, limit: 100});
      this.orderItemTableSubscriptionStrategy.subscribe(this.orderItemTableDatasink, {...ShoppingCartDao.DEFAULT_OPTIONS, limit: 0});
    }

    get itemData(){
      return this.orderItemsDataSink.rows;
    }
    get schema(){
      return this.orderItemTableDatasink.schema;
    }
    getColumn(columnId){
      return this.orderItemTableDatasink.getColumn(columnId);
    }

    get shoppingCartSizeObservable(){
      return this.shoppingCartItemsQuantity;
    }

    createReportContext(reportId){
      const {customerId} = this;
      const parameters = {
        customerId
      };
      return {
        reportId,
        parameters
      };
    }
  
    async addItemtoCart(productId, quantity){
      let cartRowEvent;
      const existingRow = this.getProductRow(productId);
      if (existingRow !== undefined){
        Logger.info(`Updating cart row ${existingRow.rowId}`);
        cartRowEvent = this.createUpdateCartRowEvent(existingRow.rowId, {quantity: parseInt(existingRow.quantity, 10) + parseInt(quantity, 10)});
      } else {
        Logger.info('Adding item to cart');
        cartRowEvent = this.createAddOrderItemRowEvent(productId, quantity);
      }

      const clientTablEventPromise = new ClientTableEventPromise(this.orderItemsDataSink, [cartRowEvent]);
      this.viewserverClient.editTable(FieldMappings.ORDER_ITEM_TABLE_NAME, this.orderItemsDataSink, [cartRowEvent], clientTablEventPromise);
      const modifiedRows = await clientTablEventPromise;
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return modifiedRows;
    }

    async purchaseCartItems(orderId){
      const rowEvents = this.itemData.map(i => this.createUpdateCartRowEvent(i.rowId, {orderId}));
      const clientTablEventPromise = new ClientTableEventPromise(this.orderItemsDataSink, rowEvents);
      this.viewserverClient.editTable(FieldMappings.ORDER_ITEM_TABLE_NAME, this.orderItemsDataSink, rowEvents, clientTablEventPromise);
      await clientTablEventPromise;
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
      return this.itemData.find(r => r.productId === productId);
    }
}
