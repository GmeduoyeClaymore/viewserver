import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from '../../common/dataSinks/RxDataSink';

export default class CartSummaryDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 1,
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...CartSummaryDaoContext.OPTIONS, ...options};
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'cartSummaryDao';
  }

  getReportContext(userId){
    return {
      reportId: 'cartSummary',
      parameters: {
        userId
      }
    };
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    const summaryRow = dataSink.rows[0];
    return summaryRow !== undefined ? {totalPrice: summaryRow.totalPrice, totalQuantity: summaryRow.totalQuantity} : {totalPrice: 0, totalQuantity: 0};
  }

  createSubscriptionStrategy({userId}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(userId), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.userId != newOptions.userId;
  }

  transformOptions(options){
    if (typeof options.userId === 'undefined'){
      throw new Error('userId should be defined');
    }
    return options;
  }
}

