import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/dataSinks/DataSink';
import Logger from '../../viewserver-client/Logger';
import CoolRxDataSink from '../../common/dataSinks/CoolRxDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import {OrderStatuses} from '../../common/constants/OrderStatuses';
import uuidv4 from 'uuid/v4';
import moment from 'moment';

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
      this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.ORDER_TABLE_NAME);
      this.viewserverClient = viewserverClient;
      this.customerId = customerId;
      this.subscriptionStrategy.subscribe(this, OrderDao.DEFAULT_OPTIONS(this.customerId));
    }
  
    async createOrder(order){
      //create order object
      const orderId = uuidv4();
      Logger.info(`Creating order ${orderId}`);
      order.orderId = orderId;

      const addOrderRowEvent = this.createAddOrderRowEvent(order);
      await this.subscriptionStrategy.editTable(this, [addOrderRowEvent]);
      Logger.info('Create order promise resolved');
      return orderId;
    }

    createAddOrderRowEvent(order){
      //note we are using UTC offset time format here
      const created = moment().format('x');

      return {
        type: 0, // ADD
        columnValues: {
          customerId: this.customerId,
          created,
          lastModified: created,
          status: OrderStatuses.PLACED,
          ...order
        }
      };
    }
}
