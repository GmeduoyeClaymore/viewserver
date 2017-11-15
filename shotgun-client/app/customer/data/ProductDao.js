import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import RxDataSink from '../../common/dataSinks/RxDataSink';

export default class ProductDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    columnsToSort: [{name: 'name', direction: 'asc'}]
  };

  constructor(client, options) {
    this.client = client;
    this.options = {...OPTIONS, ...options};
  }

  get defaultOptions(){
      this.options;
  }

  get name(){
      return 'productDao';
  }

  createDataSink(){
      return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      product: {
        products: dataSink.rows
      }
    };
  }

  createSubscriptionStrategy(){
      return new DataSourceSubscriptionStrategy(client, 'product');
  }

  doesSubscriptionNeedToBeRecreated(previousOptions){
      return !previousOptions;
  }

  transformOptions(options){
    return options;
  }
}
