import RxDataSink from '../../common/dataSinks/RxDataSink';
import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';

export default class ProductCategoryDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    parentCategoryId: undefined,
    columnsToSort: [{name: 'path', direction: 'asc'}]
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

  getReportContext(parentCategoryId, expandedCategoryIds = []){
    return {
      reportId: 'productCategory',
      dimensions: {
        dimension_parentCategoryId: this.addCategoriyIdIfDoesntContain(expandedCategoryIds, parentCategoryId)
      }
    };
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(dataSink){
    return {
      product: {
        categories: dataSink.rows
      }
    };
  }

  addCategoriyIdIfDoesntContain(collection, categoryId){
    if (typeof categoryId === 'undefined'){
      return collection;
    }
    if (!~collection.indexOf(categoryId)){
      return [categoryId, ...collection];
    }
    return collection;
  }

  createSubscriptionStrategy({parentCategoryId, expandedCategoryIds}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(parentCategoryId, expandedCategoryIds), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.parentCategoryId != newOptions.parentCategoryId || previousOptions.expandedCategoryIds != newOptions.expandedCategoryIds;
  }

  transformOptions(options){
    if (typeof options.parentCategoryId === 'undefined'){
      throw new Error('parentCategoryId should be defined');
    }
    const {searchText } = options;
    const filterExpression = searchText ? `category like \"*${searchText}*\"` : 'true == true';
    return {...options, filterExpression};
  }
}
