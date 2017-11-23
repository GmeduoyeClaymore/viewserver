import * as FieldMappings from 'common/constants/TableNames';
import RxDataSink from 'common/dataSinks/RxDataSink';
import Logger from 'common/Logger';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import uuidv4 from 'uuid/v4';
import moment from 'moment';

export const createAddDeliveryRowEvent = (delivery) => {
  return {
    type: 0, // ADD
    columnValues: {
      ...delivery
    }
  };
};
export default class DeliveryDaoContext{
  constructor(client, options = {}) {
    this.client = client;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 1000,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
    return 'delivery';
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      delivery: dataSink.rows
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, FieldMappings.DELIVERY_TABLE_NAME, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions/*, newOptions*/){
    return !previousOptions;
  }

  transformOptions(options){
    const {customerId} = options;
    if (typeof customerId === 'undefined'){
      throw new Error('customerId should be defined');
    }
    return {...options, filterExpression: `customerIdDelivery like \"${customerId}\"`};
  }

  extendDao(dao){
    dao.createDelivery = async ({deliveryAddressId, eta, type}) =>{
      //create order object
      await dao.dataSink.waitForSchema();
      const created = moment().format('x');
      const deliveryId = uuidv4();
      const customerId = dao.options.customerId;
      if (typeof customerId === 'undefined'){
        throw new Error('customerId should be defined');
      }
      const delivery =  {deliveryId, lastModified: created, deliveryAddressId, eta, type, customerIdDelivery: customerId};
      Logger.info(`Creating delivery ${deliveryId}`);
      const addDeliveryRowEvent = createAddDeliveryRowEvent(delivery);
      const promise = dao.rowEventObservable.filter(ev => ev.row.deliveryId == deliveryId).take(1).timeoutWithError(5000, new Error(`Could not detect modification to delivery ${deliveryId} created in 5 seconds`)).toPromise();
      await dao.subscriptionStrategy.editTable([addDeliveryRowEvent]);
      await promise;
      Logger.info('Delivery created');
      return deliveryId;
    };
  }
}

