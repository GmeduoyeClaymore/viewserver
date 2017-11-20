import RxDataSink from 'common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import * as FieldMappings from 'common/constants/TableNames';
import {forEach} from 'lodash';
import uuidv4 from 'uuid/v4';

const createAddDeliveryAddressEvent = (deliveryAddress) =>{
  return {
    type: 0, // ADD
    columnValues: {
      ...deliveryAddress
    }
  };
};

const createUpdateDeliveryAddressEvent = (deliveryAddress) => {
  return {
    type: 1, // UPDATE
    columnValues: {
      ...deliveryAddress
    }
  };
};

export default class DeliveryAddressDaoContext{
  constructor(client, options = {}) {
    this.client = client;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 100,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
      return 'deliveryAddresses';
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

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, FieldMappings.DELIVERY_ADDRESS_TABLE_NAME, dataSink);
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
    dao.addOrUpdateDeliveryAddress = async ({customerId, deliveryAddress}) => {
      const {dataSink, subscriptionStrategy} = dao;
      const schema = await dataSink.waitForSchema();
      const customer = dataSink.rows[0];
      let deliveryAddressRowEvent;
      Logger.info(`Adding deliveryAddress schema is ${JSON.stringify(schema)}`);
      const deliveryAddressObject = {};
      forEach(schema, value => {
        const field = value.name;
        deliveryAddressObject[field] = deliveryAddress[field];
      });
  
      deliveryAddressObject.customerId = customerId;
  
      if (deliveryAddressObject.deliveryAddressId == undefined) {
        deliveryAddressObject.deliveryAddressId = uuidv4();
      }
  
      if (customer == undefined){
        Logger.info(`Adding deliveryAddress ${JSON.stringify(deliveryAddressObject)}`);
        deliveryAddressRowEvent = createAddDeliveryAddressEvent(deliveryAddressObject);
      } else {
        Logger.info(`Updating deliveryAddress ${JSON.stringify(deliveryAddressObject)}`);
        deliveryAddressRowEvent = createUpdateDeliveryAddressEvent(deliveryAddressObject);
      }
      const promise = dao.rowEventObservable.filter(ev => ev.row.deliveryAddressId == deliveryAddressObject.deliveryAddressId).take(1).timeoutWithError(5000, new Error(`Could not detect created delivery address id ${deliveryAddressObject.deliveryAddressId} in 5 seconds`)).toPromise();
      const modifiedRows = await subscriptionStrategy.editTable([deliveryAddressRowEvent]);
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      await promise;
      return modifiedRows;
    };
  }
}
