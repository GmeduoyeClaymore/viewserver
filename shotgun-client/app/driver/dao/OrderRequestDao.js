import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';

export default class OrderRequestDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    isCompleted: ''
  };

  constructor(client, options = {}) {
    this.client = client;
    this.options = {...OrderRequestDaoContext.OPTIONS, ...options};
    this.subscribeOnCreate = false;
  }

  get defaultOptions(){
    return this.options;
  }

  get name(){
    return 'orderRequestDao';
  }

  getReportContext({contentTypeId, vehicleTypeId, noRequiredForOffload, driverLatitude, driverLongitude, maxDistance}){
    return {
      reportId: 'orderRequest',
      parameters: {
        contentTypeId,
        vehicleTypeId,
        noRequiredForOffload,
        driverLatitude,
        driverLongitude,
        maxDistance
      }
    };
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      driver: {
        orders: dataSink.rows.map(r => this.mapOrderRequest(r)),
      }
    };
  }

  mapOrderRequest(orderRequest){
    return {
      orderId: orderRequest.orderId,
      status: orderRequest.status,
      totalPrice: orderRequest.totalPrice,
      orderItem: {
        productId: orderRequest.productId,
        notes: orderRequest.notes,
        imageUrl: orderRequest.imageUrl,
      },
      contentType: {
        contentTypeId: orderRequest.contentTypeContentTypeId,
        name: orderRequest.contentTypeName,
        origin: orderRequest.contentTypeOrigin,
        destination: orderRequest.contentTypeDestination,
        noPeople: orderRequest.contentTypeNoPeople,
        fromTime: orderRequest.contentTypeFromTime,
        tillTime: orderRequest.contentTypeTillTime,
        noItems: orderRequest.contentTypeNoItems,
        hasVehicle: orderRequest.contentTypeHasVehicle,
        rootProductCategory: orderRequest.contentTypeRootProductCategory,
        pricingStrategy: orderRequest.contentTypePricingStrategy,
        defaultProductId: orderRequest.defaultProductId,
      },
      delivery: {
        from: orderRequest.from,
        till: orderRequest.till,
        noRequiredForOffload: orderRequest.noRequiredForOffload,
        deliveryId: orderRequest.deliveryId,
        distance: orderRequest.distance,
        origin: {
          flatNumber: orderRequest.originFlatNumber,
          line1: orderRequest.originLine1,
          city: orderRequest.originCity,
          postCode: orderRequest.originPostCode,
          latitude: orderRequest.originLatitude,
          longitude: orderRequest.originLongitude
        },
        destination: {
          flatNumber: orderRequest.destinationFlatNumber,
          line1: orderRequest.destinationLine1,
          city: orderRequest.destinationCity,
          postCode: orderRequest.destinationPostCode,
          latitude: orderRequest.destinationLatitude,
          longitude: orderRequest.destinationLongitude
        }
      }
    };
  }

  createSubscriptionStrategy({contentTypeId, vehicleTypeId, noRequiredForOffload, driverLatitude, driverLongitude, maxDistance}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext({contentTypeId, vehicleTypeId, noRequiredForOffload, driverLatitude, driverLongitude, maxDistance}), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    //TODO - probably needs to be updated as driver position changed but not sure how often
    return !previousOptions || previousOptions.contentTypeId != newOptions.contentTypeId;
  }

  transformOptions(options){
    if (typeof options.contentTypeId === 'undefined'){
      throw new Error('productId should be defined');
    }
    if (typeof options.vehicleTypeId === 'undefined'){
      throw new Error('vehicleTypeId  should be defined');
    }
    if (typeof options.driverLatitude === 'undefined'){
      throw new Error('driverLatitude  should be defined');
    }
    if (typeof options.driverLongitude === 'undefined'){
      throw new Error('driverLongitude  should be defined');
    }
    if (typeof options.maxDistance === 'undefined'){
      throw new Error('maxDistance  should be defined');
    }
    return options;
  }
}
