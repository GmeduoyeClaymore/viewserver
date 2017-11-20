import * as FieldMappings from 'common/constants/TableNames';
import RxDataSink from '../../common/dataSinks/RxDataSink';
import Logger from 'common/Logger';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import uuidv4 from 'uuid/v4';
import moment from 'moment';


const createAddOrderRowEvent = (order) => {
  return {
    type: 0, // ADD
    columnValues: {
      ...order
    }
  };
};
export default class OrdersDaoContext{
  constructor(client, options = {}) {
    this.client = client;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 1,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
      return 'orderDao';
  }

  createDataSink(){
      return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      orders: dataSink.rows
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, FieldMappings.ORDER_ITEM_TABLE_NAME, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.customerId != newOptions.customerId;
  }

  transformOptions(options){
    const {customerId} = options;
    if (typeof customerId === 'undefined'){
      throw new Error('customerId should be defined');
    }
    return {...options, filterExpression: `customerId == \"${customerId}\"`};
  }

  extendDao(dao){
    dao.createOrder = async ({deliveryId, paymentId}) => {
      const orderId = uuidv4();
      const created = moment().format('x');
      Logger.info(`Creating order ${orderId}`);
      const order = {orderId, created, lastModified: created, customerId: dao.options.customerId, deliveryId, paymentId};
      const addOrderRowEvent = createAddOrderRowEvent(order);
      const promise = dao.rowEventObservable.filter(ev => ev.row.orderId == orderId).take(1).timeoutWithError(5000, new Error('Could not detect created order in 5 seconds')).toPromise();
      await dao.subscriptionStrategy.editTable([addOrderRowEvent]);
      await promise;
      Logger.info('Order created');
      return orderId;
    };
  }
}

