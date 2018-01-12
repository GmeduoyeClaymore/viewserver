import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';

export default class OrderRequestDaoContext{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    isCompleted: false
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

  getReportContext({productId, vehicleTypeId, noRequiredForOffload, driverLatitude, driverLongitude, maxDistance}){
    return {
      reportId: 'orderRequest',
      parameters: {
        productId,
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
      orderItem: {
        productId: orderRequest.productId,
        notes: orderRequest.notes,
        imageUrl: orderRequest.imageUrl,
      },
      delivery: {
        eta: orderRequest.eta,
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

  createSubscriptionStrategy({productId, vehicleTypeId, noRequiredForOffload, driverLatitude, driverLongitude, maxDistance}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext({productId, vehicleTypeId, noRequiredForOffload, driverLatitude, driverLongitude, maxDistance}), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    //TODO - probably needs to be updated as driver position changed but not sure how often
    return !previousOptions || previousOptions.productId != newOptions.productId;
  }

  transformOptions(options){
    if (typeof options.productId === 'undefined'){
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
