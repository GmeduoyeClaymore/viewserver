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
    columnsToSort: [{ name: 'requiredDate', direction: 'desc' }],
    reportId: 'partnerOrderSummary',
    userId: undefined
  };

  static CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS = {
    columnsToSort: [{ name: 'requiredDate', direction: 'desc' }],
    reportId: 'customerOrderSummary',
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

  createDataSink = () => {
    return new RxDataSink(this._name);
  }

  mapDomainEvent(dataSink){
    return {
      orders: dataSink.orderedRows.map(r => this.mapOrderSummary(r))
    };
  }

  mapOrderSummary(orderRow){
    const {
      partnerResponses,
      orderDetails,
    } = orderRow;

    let result = this.addCustomerInfo(orderDetails, orderRow);
    result = this.addResponseInfo(result, orderRow);
    result = this.addPartnerInfo(result, orderRow);
    const partnerResponsesMapped = !partnerResponses ? null : Object.values(partnerResponses).map(this.mapPartnerResponse);
    return {partnerResponses: partnerResponsesMapped, ...result};
  }

  addResponseInfo(orderDetails, orderRow){
    const responseInfo = {
      responseStatus: orderRow.responseStatus,
      responsePrice: orderRow.responsePrice,
      responseDate: orderRow.responseDate,
    };
    const result = {...orderDetails};
    result.responseInfo = responseInfo;
    return result;
  }

  addCustomerInfo(orderDetails, orderRow){
    const customerInfo  = {
      latitude: orderRow.customer_latitude,
      longitude: orderRow.customer_longitude,
      firstName: orderRow.customer_firstName,
      lastName: orderRow.customer_lastName,
      email: orderRow.customer_email,
      imageUrl: orderRow.customer_imageUrl,
      online: orderRow.customer_online,
      userStatus: orderRow.customer_userStatus,
      statusMessage: orderRow.customer_statusMessage,
      ratingAvg: orderRow.customer_ratingAvg,
    };
    return {...orderDetails, customer: customerInfo};
  }

  addPartnerInfo(orderDetails, orderRow){
    const {assignedPartner} = orderDetails;
    if (!assignedPartner){
      return orderDetails;
    }
    const partnerInfo  = {
      latitude: orderRow.partner_latitude,
      longitude: orderRow.partner_longitude,
      firstName: orderRow.partner_firstName,
      lastName: orderRow.partner_lastName,
      email: orderRow.partner_email,
      imageUrl: orderRow.partner_imageUrl,
      online: orderRow.partner_online,
      userStatus: orderRow.partner_userStatus,
      statusMessage: orderRow.partner_statusMessage,
      ratingAvg: orderRow.partner_ratingAvg,
    };

    const newAssignedPartner = {
      ...assignedPartner,
      ...partnerInfo,
    };

    return {...orderDetails, assignedPartner: newAssignedPartner};
  }

  mapPartnerResponse(response){
    return {
      partnerId: response.spreadPartnerId,
      latitude: response.partner_latitude,
      longitude: response.partner_longitude,
      firstName: response.partner_firstName,
      lastName: response.partner_lastName,
      email: response.partner_email,
      imageUrl: response.partner_imageUrl,
      online: response.partner_online,
      userStatus: response.partner_userStatus,
      statusMessage: response.partner_statusMessage,
      ratingAvg: response.partner_ratingAvg,
      estimatedDate: response.estimatedDate,
      price: response.price,
      responseStatus: response.responseStatus,
    };
  }

  createSubscriptionStrategy({partnerId, isCompleted, reportId, orderId, userId, selectedProducts}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext({partnerId, userId, reportId, isCompleted, orderId, selectedProducts}), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || hasAnyOptionChanged(previousOptions, newOptions, ['orderId', 'isCompleted', 'reportId', 'partnerId', 'selectedProducts']);
  }

  transformOptions(options){
    if (typeof options.reportId === 'undefined' || (options.reportId !== 'customerOrderSummary' && options.reportId !== 'partnerOrderSummary' && options.reportId !== 'partnerOrderResponse')){
      throw new Error('reportId should be defined and be either customerOrderSummary or partnerOrderSummary');
    }

    return options;
  }
}
