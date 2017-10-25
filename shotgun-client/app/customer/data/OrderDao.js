import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/DataSink';
import Logger from '../../viewserver-client/Logger';
import ClientTableEventPromise from '../../common/ClientTableEventPromise';
import CoolRxDataSink from '../../common/CoolRxDataSink';
import OperatorSubscriptionStrategy from '../../common/OperatorSubscriptionStrategy';
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
      this.subscribeToData(this);
    }

    subscribeToData(datasink){
      this.subscriptionStrategy.subscribe(datasink, OrderDao.DEFAULT_OPTIONS(this.customerId));
    }
  
    async createOrder(){
      const clientTableEventPromise = new ClientTableEventPromise(this);

      //create order object
      const orderId = uuidv4();
      Logger.info(`Creating order ${orderId}`);
      const addOrderRowEvent = this.createAddOrderRowEvent(orderId);
      this.viewserverClient.editTable(FieldMappings.ORDER_TABLE_NAME, this, [addOrderRowEvent], clientTableEventPromise);
      await clientTableEventPromise;
      Logger.info('Create order promise resolved');
      return orderId;
    }

    createAddOrderRowEvent(orderId){
      const created = new Date();

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
