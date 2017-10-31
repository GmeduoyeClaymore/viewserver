import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/dataSinks/DataSink';
import Logger from '../../viewserver-client/Logger';
import TableEditPromise from '../../common/promises/TableEditPromise';
import CoolRxDataSink from '../../common/dataSinks/CoolRxDataSink';
import OperatorSubscriptionStrategy from '../../common/subscriptionStrategies/OperatorSubscriptionStrategy';
import {OrderStatuses} from '../../common/constants/OrderStatuses';
import uuidv4 from 'uuid/v4';

export default class OrderDao extends DataSink(CoolRxDataSink){
    static DEFAULT_OPTIONS = (customerId) =>  {
      return {
        offset: 0,
        limit: 20,
        columnName: undefined,
        columnsToSort: undefined,
        filterMode: 2, //Filtering
        filterExpression: `customerId == "${customerId}"`,
        flags: undefined
      };
    };
  
    constructor(viewserverClient, customerId){
      super();
      this.subscriptionStrategy = new OperatorSubscriptionStrategy(viewserverClient, FieldMappings.ORDER_TABLE_NAME);
      this.viewserverClient = viewserverClient;
      this.customerId = customerId;
      this.subscriptionStrategy.subscribe(this, OrderDao.DEFAULT_OPTIONS(this.customerId));
    }
  
    async createOrder(){
      //create order object
      const orderId = uuidv4();
      Logger.info(`Creating order ${orderId}`);
      const addOrderRowEvent = this.createAddOrderRowEvent(orderId);
      const tableEditPromise = new TableEditPromise();
      this.viewserverClient.editTable(FieldMappings.ORDER_TABLE_NAME, this, [addOrderRowEvent], tableEditPromise);
      await tableEditPromise;
      Logger.info('Create order promise resolved');
      return orderId;
    }

    createAddOrderRowEvent(orderId){
      //const created = new Date();

      return {
        type: 0, // ADD
        columnValues: {
          orderId,
          customerId: this.customerId,
          /*  created,
          lastModified: created,*/
          status: OrderStatuses.PLACED
        }
      };
    }
}
