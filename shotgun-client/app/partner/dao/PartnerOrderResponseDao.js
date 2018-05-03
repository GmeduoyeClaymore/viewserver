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
      //TODO - use the OrderStatus enum for these values
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
      orders: dataSink.orderedRows.map(this.mapOrderResponse)
    };
  }

  mapOrderResponse(orderResponse){
    const {orderDetails} = orderResponse;

    const assignedPartner = orderDetails.assignedPartner ? {
      ...orderDetails.assignedPartner,
      firstName: orderResponse.partner_firstName,
      lastName: orderResponse.partner_lastName,
      ratingAvg: orderResponse.partner_ratingAvg,
      imageUrl: orderResponse.partner_imageUrl
    } : undefined;

    return {
      ...orderDetails,
      assignedPartner,
      userCreatedThisOrder: orderResponse.userCreatedThisOrder || false,
      customer: {
        userId: orderResponse.customer_userId,
        firstName: orderResponse.customer_firstName,
        lastName: orderResponse.customer_lastName,
        ratingAvg: orderResponse.customer_ratingAvg
      }
    };
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
