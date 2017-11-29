import DataSourceSubscriptionStrategy from '../subscriptionStrategies/DataSourceSubscriptionStrategy';
import RxDataSink from '../dataSinks/RxDataSink';

export default class VehicleTypeDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 100,
    filterMode: 2
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...VehicleTypeDaoContext.OPTIONS, ...options};
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'vehicleTypeDao';
  }

  createDataSink(){
    return new RxDataSink();
  }

  transformOptions(options){
    return options;
  }

  mapDomainEvent(event, dataSink){
    return {
      vehicleTypes: dataSink.rows
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, 'vehicleType', dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions){
    return !previousOptions;
  }
}
