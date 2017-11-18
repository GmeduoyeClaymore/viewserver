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
      this.options;
  }

  get name(){
      return 'cartSummary';
  }

  getReportContext({customerId}){
    return {
      reportId: 'cartSummary',
      parameters: {
        customerId
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

  createSubscriptionStrategy({customerId}){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext(customerId));
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.customerId != newOptions.customerId;
  }

  transformOptions(options){
    if (typeof options.parentCategoryId === 'undefined'){
      throw new Error('Parent category should be defined');
    }
  }
}

