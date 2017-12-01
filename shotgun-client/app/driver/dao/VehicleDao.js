import * as TableNames from 'common/constants/TableNames';
import RxDataSink from 'common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';
import {forEach} from 'lodash';
import uuidv4 from 'uuid/v4';

const createAddVehicleEvent = (vehicle) => {
  return {
    type: 0, // ADD
    columnValues: {
      ...vehicle
    }
  };
};

const createUpdateVehicleEvent = (vehicle) =>{
  return {
    type: 1, // UPDATE
    columnValues: {
      ...vehicle
    }
  };
};

export default class VehiclesDaoContext{
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
      flags: undefined,
      ...this.options
    };
  }

  get name(){
    return 'vehiclesDao';
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      customer: {
        vehicles: dataSink.rows
      }
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, TableNames.VEHICLE_TABLE_NAME, dataSink);
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
    dao.addOrUpdateVehicle = async({userId, vehicle}) =>{
      const {dataSink, subscriptionStrategy} = dao;
      const schema = await dataSink.waitForSchema();
      let vehicleRowEvent;
      const existingVehicle = dataSink.rows[0];
  
      Logger.info(`Adding vehicle schema is ${JSON.stringify(schema)}`);
      const vehicleObject = {};
      forEach(schema, value => {
        const field = value.name;
        vehicleObject[field] = vehicle[field];
      });
  
      vehicleObject.userId = userId;
  
      if (vehicleObject.vehicleId == undefined) {
        vehicleObject.vehicleId = uuidv4();
      }
  
      if (existingVehicle == undefined){
        Logger.info(`Adding vehicle ${JSON.stringify(vehicleObject)}`);
        vehicleRowEvent = createAddVehicleEvent(vehicleObject);
      } else {
        Logger.info(`Updating vehicle ${JSON.stringify(vehicleObject)}`);
        vehicleRowEvent = createUpdateVehicleEvent(vehicleObject);
      }
      const promise = dao.rowEventObservable.filter(ev => ev.row.vehicleId == vehicleObject.vehicleId).take(1).timeoutWithError(5000, new Error(`Could not detect creation of vehicle ${vehicleObject.vehicleId} in 5 seconds`)).toPromise();
      const modifiedRows = await subscriptionStrategy.editTable([vehicleRowEvent]);
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      return await promise;
    };
  }
}

