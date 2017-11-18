import RxDataSink from 'common/dataSinks/RxDataSink';

export default class OrderSummaryDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    columnsToSort: [{name: 'category', direction: 'asc'}]
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...OrderSummaryDaoContext.OPTIONS, ...options};
  }

  get defaultOptions(){
      this.options;
  }

  get name(){
      return 'orderSummary';
  }

  getReportContext({customerId, isCompleted}){
    return {
      reportId: 'orderSummary',
      parameters: {
        customerId,
        isCompleted
      }
    };
  }

  createDataSink(){
      return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      customer: {
        orders: {
          //todo work out exactly why we need this cruft :)
          ['incomplete']: dataSink.rows
        }
      }
    };
  }

  createSubscriptionStrategy({customerId, isCompleted}){
    return new ReportSubscriptionStrategy(client, this.getReportContext({customerId, isCompleted}));
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.customerId != newOptions.customerId || previousOptions.isCompleted != newOptions.isCompleted;
  }

  transformOptions(options){
    if (typeof options.parentCategoryId === 'undefined'){
      throw new Error('Parent category should be defined');
    }
  }
}
