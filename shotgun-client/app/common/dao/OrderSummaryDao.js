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
    columnsToSort: [{ name: 'startTime', direction: 'asc' }, { name: 'orderId', direction: 'asc' }],
    reportId: 'partnerOrderSummary',
    partnerId: '@userId',
    userId: undefined
  };

  static CUSTOMER_ORDER_SUMMARY_DEFAULT_OPTIONS = {
    columnsToSort: [{ name: 'startTime', direction: 'asc' }, { name: 'orderId', direction: 'asc' }],
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
      dimensions: {},
      excludedFilters: {}
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
    return {
      orderId: orderSummary.orderId,
      status: orderSummary.status,
      customerUserId: orderSummary.customerUserId,
      rank: orderSummary.rank,
      totalPrice: orderSummary.totalPrice,
      customerRating: orderSummary.customerRating,
      partnerRating: orderSummary.partnerRating,
      quantity: orderSummary.quantity,
      orderItem: {
        productId: orderSummary.productId,
        notes: orderSummary.notes,
        imageUrl: orderSummary.imageUrl,
        fixedPrice: orderSummary.fixedPrice == -1 ? undefined : orderSummary.fixedPrice,
        startTime: orderSummary.startTime,
        endTime: orderSummary.endTime
      },
      product: {
        productId: orderSummary.productProductId,
        name: orderSummary.productName,
        imageUrl: orderSummary.productImageUrl
      },
      contentType: {
        contentTypeId: orderSummary.contentTypeContentTypeId,
        name: orderSummary.contentTypeName,
        hasOrigin: orderSummary.contentTypeHasOrigin,
        hasDestination: orderSummary.contentTypeHasDestination,
        hasStartTime: orderSummary.contentTypeHasStartTime,
        hasEndTime: orderSummary.contentTypeHasEndTime,
        rootProductCategory: orderSummary.contentTypeRootProductCategory,
        pricingStrategy: orderSummary.contentTypePricingStrategy
      },
      delivery: {
        distance: orderSummary.distance,
        duration: orderSummary.duration,
        deliveryId: orderSummary.deliveryId,
        customerRatingAvg: orderSummary.customerRatingAvg != undefined ? orderSummary.customerRatingAvg : 0,
        customerFirstName: orderSummary.customerFirstName,
        customerLastName: orderSummary.customerLastName,
        partnerRatingAvg: orderSummary.partnerRatingAvg != undefined ? orderSummary.partnerRatingAvg : 0,
        partnerFirstName: orderSummary.partnerFirstName,
        partnerLastName: orderSummary.partnerLastName,
        partnerImageUrl: orderSummary.partnerImageUrl,
        partnerLatitude: orderSummary.partnerLatitude,
        partnerLongitude: orderSummary.partnerLongitude,
        registrationNumber: orderSummary.registrationNumber,
        vehicleColour: orderSummary.vehicleColour,
        vehicleMake: orderSummary.vehicleMake,
        vehicleModel: orderSummary.vehicleModel,
        origin: {
          flatNumber: orderSummary.originFlatNumber,
          line1: orderSummary.originLine1,
          city: orderSummary.originCity,
          postCode: orderSummary.originPostCode,
          latitude: orderSummary.originLatitude,
          longitude: orderSummary.originLongitude
        },
        destination: {
          flatNumber: orderSummary.destinationFlatNumber,
          line1: orderSummary.destinationLine1,
          city: orderSummary.destinationCity,
          postCode: orderSummary.destinationPostCode,
          latitude: orderSummary.destinationLatitude,
          longitude: orderSummary.destinationLongitude
        }
        
      },
      payment: {
        paymentId: orderSummary.paymentId
      }
    };
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
