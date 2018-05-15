import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';
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

  getReportContext({categoryId}){
    return {
      reportId: 'productReport',
      parameters: {
        categoryId
      }
    };
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
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(options), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return hasAnyOptionChanged(previousOptions, newOptions, ['categoryId']);
  }

  transformOptions(options){
    const {searchText, categoryId} = options;
    if (!categoryId){
      throw new Error('Category id must be specified');
    }
    const filterExpression = searchText ? `name like "*${searchText}*"` : undefined;
    return {...options, filterExpression};
  }
}
