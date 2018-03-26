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

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...OrderSummaryDao.OPTIONS, ...options};
    this.subscribeOnCreate = false;
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'orderSummaryDao';
  }

  getReportContext({orderId, isCompleted, reportId, driverId, selectedProducts, userId}){
    const reportContext =  {
      reportId,
      dimensions: {
      }
    };

    if (isCompleted !== undefined) {
      reportContext.dimensions.dimension_status = isCompleted == true ? [OrderStatuses.COMPLETED] : [OrderStatuses.ACCEPTED, OrderStatuses.PLACED, OrderStatuses.PICKEDUP];
    }

    if (orderId !== undefined){
      reportContext.dimensions.dimension_orderId = orderId;
    }

    if (userId != undefined){
      reportContext.dimensions.dimension_userId = userId;
    }
    if (driverId !== undefined){
      reportContext.dimensions.dimension_driverId = driverId;
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
      rank: orderSummary.rank,
      totalPrice: orderSummary.totalPrice,
      customerRating: orderSummary.customerRating,
      driverRating: orderSummary.driverRating,
      quantity: orderSummary.quantity,
      orderItem: {
        productId: orderSummary.productId,
        notes: orderSummary.notes,
        imageUrl: orderSummary.imageUrl,
      },
      product: {
        productId: orderSummary.productProductId,
        name: orderSummary.productName,
        imageUrl: orderSummary.productImageUrl
      },
      contentType: {
        contentTypeId: orderSummary.contentTypeContentTypeId,
        name: orderSummary.contentTypeName,
        origin: orderSummary.contentTypeOrigin,
        destination: orderSummary.contentTypeDestination,
        noPeople: orderSummary.contentTypeNoPeople,
        fromTime: orderSummary.contentTypeFromTime,
        tillTime: orderSummary.contentTypeTillTime,
        noItems: orderSummary.contentTypeNoItems,
        rootProductCategory: orderSummary.contentTypeRootProductCategory,
        pricingStrategy: orderSummary.contentTypePricingStrategy
      },
      delivery: {
        from: orderSummary.from,
        till: orderSummary.till,
        distance: orderSummary.distance,
        isFixedPrice: orderSummary.isFixedPrice,
        fixedPriceValue: orderSummary.fixedPriceValue,
        duration: orderSummary.duration,
        noRequiredForOffload: orderSummary.noRequiredForOffload,
        deliveryId: orderSummary.deliveryId,
        customerRatingAvg: orderSummary.customerRatingAvg != undefined ? orderSummary.customerRatingAvg.toFixed(1) : undefined,
        customerFirstName: orderSummary.customerFirstName,
        customerLastName: orderSummary.customerLastName,
        driverRatingAvg: orderSummary.driverRatingAvg != undefined ? orderSummary.driverRatingAvg.toFixed(1) : undefined,
        driverFirstName: orderSummary.driverFirstName,
        driverLastName: orderSummary.driverLastName,
        driverImageUrl: orderSummary.driverImageUrl,
        driverLatitude: orderSummary.driverLatitude,
        driverLongitude: orderSummary.driverLongitude,
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

  createSubscriptionStrategy({driverId, isCompleted, reportId, orderId, userId, selectedProducts}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext({driverId, userId, reportId, isCompleted, orderId, selectedProducts}), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || hasAnyOptionChanged(previousOptions, newOptions, ['orderId', 'isCompleted', 'reportId', 'driverId', 'selectedProducts']);
  }

  transformOptions(options){
    if (typeof options.reportId === 'undefined' || (options.reportId !== 'customerOrderSummary' && options.reportId !== 'driverOrderSummary')){
      throw new Error('reportId should be defined and be either customerOrderSummary or driverOrderSummary');
    }

    return options;
  }
}
