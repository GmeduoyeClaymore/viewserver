import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import RxDataSink from '../../common/dataSinks/RxDataSink';

export default class ProductDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    columnsToSort: [{name: 'name', direction: 'asc'}]
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...ProductDaoContext.OPTIONS, ...options};
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'productDao';
  }

  getDataFrequency(){
    return 50;
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(dataSink){
    return {
      product: {
        products: dataSink.rows
      }
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, 'product', dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions){
    return !previousOptions;
  }

  transformOptions(options){
    const {categoryId, searchText } = options;
    const categoryFilterExpression = categoryId ? `categoryId like \"${categoryId}\"` : 'true == true';
    const productFilter = searchText ? `name like "*${searchText}*"` : '';
    const filterExpression = productFilter === '' ? categoryFilterExpression : `${productFilter} && ${categoryFilterExpression}`;
    return {...options, filterExpression};
  }
}
