import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import RxDataSink from '../../common/dataSinks/RxDataSink';
import {hasAnyOptionChanged} from 'common/dao';

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

  createDataSink = () => {
    return new RxDataSink(this._name);
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

  doesDataSinkNeedToBeCleared(previousOptions, newOptions){
    return hasAnyOptionChanged(previousOptions, newOptions, ['categoryId']);
  }

  transformOptions(options){
    const {categoryId, searchText } = options;
    const categoryFilterExpression = categoryId ? `categoryId like \"${categoryId}\"` : 'true == false';
    const productFilter = searchText ? `name like "*${searchText}*"` : '';
    const filterExpression = productFilter === '' ? categoryFilterExpression : `${productFilter} && ${categoryFilterExpression}`;
    return {...options, filterExpression};
  }
}
