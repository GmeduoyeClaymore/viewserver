import DataSourceSubscriptionStrategy from '../subscriptionStrategies/DataSourceSubscriptionStrategy';
import RxDataSink from '../dataSinks/RxDataSink';

export default class ContentTypeDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 100
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...ContentTypeDaoContext.OPTIONS, ...options};
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'contentTypeDao';
  }

  createDataSink(){
    return new RxDataSink();
  }

  transformOptions(options){
    return options;
  }

  mapDomainEvent(event, dataSink){
    return {
      contentTypes: dataSink.rows
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, 'contentType', dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions){
    return !previousOptions;
  }
}
