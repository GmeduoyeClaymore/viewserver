import * as FieldMappings from '../../common/constants/TableNames';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from '../../viewserver-client/Logger';
import RxDataSink from '../../common/dataSinks/RxDataSink';

const createAddCustomerEvent = (args) => {
  return {
    type: 0, // ADD
    columnValues: args
  };
};

const createUpdateCustomerEvent = (args) => {
  return {
    type: 1, // UPDATE
    columnValues: args
  };
};

export default class OrderItemsDaoContext{
  constructor(client, options) {
    this.client = client;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      filterExpression: `customerId == "${customerId}"`,
      flags: undefined
    };
  }

  get name(){
      return 'orderItems';
  }

  createDataSink(){
      return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      customer: {
        deliveryAddresses: dataSink.rows
      }
    };
  }

  createSubscriptionStrategy(){
    return new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.CUSTOMER_TABLE_NAME);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.customerId != newOptions.customerId;
  }

  transformOptions(options){
    const {customerId} = options;
    if (typeof customerId === 'undefined'){
      throw new Error('customerId should be defined');
    }
    return {...options, filterExpression: `customerId == "${customerId}"`};
  }

  extendDao(dao){
    dao.addOrUpdateCustomer = async (args) => {
      let customerRowEvent;
      const message = {};
      const {dataSink, subscriptionStrategy} = dao;
      const {schema} = dataSink;
      const {customerId} = dao.options;
      Logger.info(`Adding customer schema is ${JSON.stringify(schema)}`);
      Object.keys(schema).forEach(key => { message[key] = args[key];});

      if (!dataSink.rows.length){
        Logger.info(`Adding customer ${JSON.stringify(message)}`);
        customerRowEvent = createAddCustomerEvent(message);
      } else {
        Logger.info(`Updating customer ${JSON.stringify(message)}`);
        customerRowEvent = createUpdateCustomerEvent(message);
      }
      const modifiedRows = await subscriptionStrategy.editTable(dataSink, [customerRowEvent]);
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      const result = await dao.rowEventObservable.filter(row => row.customerId == customerId).timeout(5000, `Could not modification to customer id ${customerId} in 5 seconds`).toPromise();
      return result;
    }
  }
}

