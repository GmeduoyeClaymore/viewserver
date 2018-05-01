import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {hasAnyOptionChanged} from 'common/dao';


export default class PartnerOrderResponseDao{
  static DEFAULT_OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    columnsToSort: [{ name: 'requiredDate', direction: 'asc' }],
    partnerId: '@userId',
    userId: undefined
  };

  constructor(client, options = {}, name = 'partnerOrderResponseDao') {
    this.client = client;
    this.options = {...PartnerOrderResponseDao.DEFAULT_OPTIONS, ...options};
    this.subscribeOnCreate = false;
    this._name = name;
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return  this._name;
  }

  getReportContext({orderId, isCompleted}){
    const reportContext =  {
      reportId: 'orderResponses',
      dimensions: {},
      excludedFilters: {}
    };

    reportContext.dimensions.dimension_partnerId = ['@userId'];

    if (isCompleted !== undefined) {
      reportContext.dimensions.dimension_partnerOrderStatus = isCompleted == true ? ['CUSTOMERCOMPLETE'] : ['REQUESTED', 'RESPONDED', 'ASSIGNED', 'STARTED', 'PARTNERCOMPLETE'];
    }

    if (orderId !== undefined){
      reportContext.dimensions.dimension_orderId = orderId;
    }

    return reportContext;
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(dataSink){
    return {
      orders: dataSink.orderedRows.map(r => this.mapOrderSummary(r))
    };
  }

  mapOrderSummary(orderSummary){
    return orderSummary;
  }

  createSubscriptionStrategy({isCompleted, orderId}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext({isCompleted, orderId}), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || hasAnyOptionChanged(previousOptions, newOptions, ['orderId', 'isCompleted']);
  }

  transformOptions(options){
    return options;
  }
}
