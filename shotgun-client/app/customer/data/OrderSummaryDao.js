import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';

export default class OrderSummaryDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    isCompleted: false
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...OrderSummaryDaoContext.OPTIONS, ...options};
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'orderSummaryDao';
  }

  getReportContext({userId, isCompleted}){
    return {
      reportId: 'orderSummary',
      parameters: {
        userId,
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
        orders: dataSink.rows,
      }
    };
  }

  createSubscriptionStrategy({userId, isCompleted}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext({userId, isCompleted}), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.userId != newOptions.userId || previousOptions.isCompleted != newOptions.isCompleted;
  }

  transformOptions(options){
    if (typeof options.userId === 'undefined'){
      throw new Error('userId should be defined');
    }
    if (typeof options.isCompleted === 'undefined'){
      throw new Error('isCompleted  should be defined');
    }
    return options;
  }
}
