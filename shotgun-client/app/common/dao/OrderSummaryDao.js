import ReportSubscriptionStrategy from '../subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {OrderStatuses} from 'common/constants/OrderStatuses';
import {hasAnyOptionChanged} from 'common/dao';


export default class OrderSummaryDao{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
  };

  static PARTNER_ORDER_SUMMARY_DEFAULT_OPTIONS = {
    columnsToSort: [{ name: 'requiredDate', direction: 'asc' }, { name: 'orderId', direction: 'asc' }],
    reportId: 'partnerOrderSummary',
    partnerId: '@userId',
    userId: undefined
  };

  static CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS = {
    columnsToSort: [{ name: 'requiredDate', direction: 'asc' }, { name: 'orderId', direction: 'asc' }],
    reportId: 'customerOrderSummary',
    userId: '@userId',
    partnerId: undefined
  };

  constructor(client, options = {}, name = 'orderSummaryDao') {
    this.client = client;
    this.options = {...OrderSummaryDao.OPTIONS, ...options};
    this.subscribeOnCreate = false;
    this._name = name;
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return  this._name;
  }

  getReportContext({orderId, isCompleted, reportId, partnerId, selectedProducts, userId}){
    const reportContext =  {
      reportId,
      dimensions: {
      },
      excludedFilters: {
      }
    };

    if (isCompleted !== undefined) {
      reportContext.dimensions.dimension_status = isCompleted == true ? [OrderStatuses.COMPLETED] : [OrderStatuses.ACCEPTED, OrderStatuses.PLACED, OrderStatuses.PICKEDUP];
    }

    if (orderId !== undefined){
      reportContext.dimensions.dimension_orderId = orderId;
    }

    if (userId != undefined){
      reportContext.dimensions.dimension_customerUserId = [userId];
    }
    if (partnerId !== undefined){
      reportContext.dimensions.dimension_partnerId = partnerId;
    }

    if (selectedProducts !== undefined){
      reportContext.dimensions.dimension_selectedProducts = selectedProducts;
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

  createSubscriptionStrategy({partnerId, isCompleted, reportId, orderId, userId, selectedProducts}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext({partnerId, userId, reportId, isCompleted, orderId, selectedProducts}), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || hasAnyOptionChanged(previousOptions, newOptions, ['orderId', 'isCompleted', 'reportId', 'partnerId', 'selectedProducts']);
  }

  transformOptions(options){
    if (typeof options.reportId === 'undefined' || (options.reportId !== 'customerOrderSummary' && options.reportId !== 'partnerOrderSummary')){
      throw new Error('reportId should be defined and be either customerOrderSummary or partnerOrderSummary');
    }

    return options;
  }
}
