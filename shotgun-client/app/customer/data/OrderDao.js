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
      return 'order';
  }

  createDataSink(){
      return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      orders: dataSink.rows
    };
  }

  createSubscriptionStrategy(){
    return new DataSourceSubscriptionStrategy(this.client, FieldMappings.ORDER_ITEM_TABLE_NAME);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.customerId != newOptions.customerId;
  }

  transformOptions(options){
    const {customerId, orderId} = options;
    if (typeof customerId === 'undefined'){
      throw new Error('customerId should be defined');
    }
    if (typeof orderId === 'undefined'){
      throw new Error('customerId should be defined');
    }
    return {...options, filterExpression: `customerId == "${customerId}"`};
  }

  extendDao(dao){
    dao.createOrder = async () => {
      const orderId = uuidv4();
      const created = moment().format('x');
      const {dataSink} = dao;
      Logger.info(`Creating order ${orderId}`);
      const order = {orderId, created, lastModified: created, customerId: dao.options.customerId};
      const addOrderRowEvent = createAddOrderRowEvent(order);
      await dao.subscriptionStrategy.editTable(dataSink, [addOrderRowEvent]);
      await dao.rowEventObservable.filter(row => row.orderId == orderId).timeout(5000, 'Could not detect created order in 5 seconds').toPromise();
      Logger.info('Order created');
      return orderId;
    };
  }
}

