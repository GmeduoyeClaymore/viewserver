import * as TableNames from 'common/constants/TableNames';
import RxDataSink from 'common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from 'common/Logger';

export default class VehiclesDaoContext{
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
      flags: undefined,
      ...this.options
    };
  }

  get name(){
    return 'vehicleDao';
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(dataSink){
    const vehicle = {...dataSink.rows[0]};
    if (vehicle.selectedProducts && typeof vehicle.selectedProducts === 'string'){
      vehicle.selectedProducts = JSON.parse(vehicle.selectedProducts);
    }
    return {
      vehicle
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, TableNames.VEHICLE_TABLE_NAME, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions){
    return !previousOptions;
  }

  transformOptions(options){
    return {...options, filterExpression: 'userId == "@userId"'};
  }

  extendDao(dao){
    dao.addOrUpdateVehicle = async({vehicle}) =>{
      const promise = this.client.invokeJSONCommand('vehicleController', 'addOrUpdateVehicle', vehicle);
      const result =  await promise.timeoutWithError(5000, new Error('Could not update vehicle in 5 seconds'));
      Logger.debug('Updated vehicle');
      return result;
    };
  }
}

