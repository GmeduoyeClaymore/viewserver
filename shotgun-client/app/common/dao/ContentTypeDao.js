import ReportSubscriptionStrategy from '../subscriptionStrategies/ReportSubscriptionStrategy';
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

  getReportContext(){
    return {
      reportId: 'contentTypeCategory',
      parameters: {
      }
    };
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

  mapDomainEvent(dataSink){
    return {
      contentTypes: dataSink.rows.map(this.createContentType)
    };
  }

  createContentType(row){
    const {contentTypeId, name, hasOrigin, hasDestination, hasStartTime, hasEndTime, pricingStrategy, description, rootProductCategory} = row;
    const {categoryId, category, parentCategoryId, isLeaf, level, path} = row;
    const productCategory = {categoryId, category, parentCategoryId, isLeaf, level, path};
    return {
      contentTypeId, name, hasOrigin, hasDestination, hasStartTime, hasEndTime, rootProductCategory, pricingStrategy, description, productCategory
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions){
    return !previousOptions;
  }
}
