import RxDataSink from 'common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import * as TableNames from 'common/constants/TableNames';
import {forEach} from 'lodash';
import uuidv4 from 'uuid/v4';
import moment from 'moment';

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
    tableKey: deliveryAddress.deliveryAddressId,
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
    return 'deliveryAddressDao';
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
    return new DataSourceSubscriptionStrategy(this.client, TableNames.DELIVERY_ADDRESS_TABLE_NAME, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.userId != newOptions.userId;
  }

  transformOptions(options){
    const {userId} = options;
    if (typeof userId === 'undefined'){
      throw new Error('userId should be defined');
    }
    return {...options, filterExpression: `userId == \"${userId}\"`};
  }

  extendDao(dao){
    dao.addOrUpdateDeliveryAddress = async ({userId, deliveryAddress}) => {
      const {dataSink, subscriptionStrategy} = dao;
      const schema = await dataSink.waitForSchema();
      const existingDeliveryAddress = dataSink.rows.find(r => r.googlePlaceId == deliveryAddress.googlePlaceId);
      const date = moment().format('x');
      let deliveryAddressRowEvent;
      Logger.info(`Adding deliveryAddress schema is ${JSON.stringify(schema)}`);
      const deliveryAddressObject = {};
      forEach(schema, value => {
        const field = value.name;
        deliveryAddressObject[field] = deliveryAddress[field];
      });
  
      deliveryAddressObject.userId = userId;

      if (existingDeliveryAddress == undefined){
        deliveryAddressObject.deliveryAddressId = uuidv4();
        deliveryAddressObject.created = date;
        deliveryAddressObject.lastUsed = date;
        Logger.info(`Adding deliveryAddress ${JSON.stringify(deliveryAddressObject)}`);
        deliveryAddressRowEvent = createAddDeliveryAddressEvent(deliveryAddressObject);
      } else {
        deliveryAddressObject.deliveryAddressId = existingDeliveryAddress.deliveryAddressId;
        deliveryAddressObject.lastUsed = date;
        Logger.info(`Updating deliveryAddress ${JSON.stringify(deliveryAddressObject)}`);
        deliveryAddressRowEvent = createUpdateDeliveryAddressEvent(deliveryAddressObject);
      }
      const result = dao.rowEventObservable.filter(ev => ev.row.deliveryAddressId == deliveryAddressObject.deliveryAddressId).take(1).timeoutWithError(5000, new Error(`Could not detect created delivery address id ${deliveryAddressObject.deliveryAddressId} in 5 seconds`)).toPromise();
      await subscriptionStrategy.editTable([deliveryAddressRowEvent]);
      const modifiedRows = await result;
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return modifiedRows.row.deliveryAddressId;
    };
  }
}
