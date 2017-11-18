import RxDataSink from '../../common/dataSinks/RxDataSink';
import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';

export default class ProductCategoryDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    parentCategoryId: 'NONE',
    columnsToSort: [{name: 'category', direction: 'asc'}]
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...ProductCategoryDaoContext.OPTIONS, ...options};
  }

  get defaultOptions(){
      return this.options;
  }

  get name(){
      return 'productCategoryDao';
  }

  getReportContext(parentCategoryId){
    return {
      reportId: 'productCategory',
      parameters: {
        parentCategoryId
      }
    };
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

  createSubscriptionStrategy({parentCategoryId}){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(parentCategoryId));
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.parentCategoryId != newOptions.parentCategoryId;
  }

  transformOptions(options){
    if (typeof options.parentCategoryId === 'undefined'){
      throw new Error('Parent category should be defined');
    }
    return options;
  }
}
