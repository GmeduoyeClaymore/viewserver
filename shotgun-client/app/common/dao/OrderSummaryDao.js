import ReportSubscriptionStrategy from '../subscriptionStrategies/ReportSubscriptionStrategy';
import RxDataSink from 'common/dataSinks/RxDataSink';
import {isEqual} from 'lodash';

export default class OrderSummaryDao{
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    isCompleted: ''
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

  getReportContext({userId, orderId, isCompleted, reportId}){
    return {
      reportId,
      parameters: {
        userId,
        isCompleted,
        orderId
      }
    };
  }


  getDataFrequency(){
    return 50;
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      orders: dataSink.rows.map(r => this.mapOrderSummary(r))
    };
  }

  mapOrderSummary(orderSummary){
    return {
      orderId: orderSummary.orderId,
      status: orderSummary.status,
      totalPrice: orderSummary.totalPrice,
      orderItem: {
        productId: orderSummary.productId,
        notes: orderSummary.notes,
        imageUrl: orderSummary.imageUrl,
      },
      delivery: {
        eta: orderSummary.eta,
        noRequiredForOffload: orderSummary.noRequiredForOffload,
        vehicleTypeId: orderSummary.vehicleTypeId,
        deliveryId: orderSummary.deliveryId,
        customerRating: orderSummary.customerRating,
        driverRating: orderSummary.driverRating,
        customerFirstName: orderSummary.customerFirstName,
        customerLastName: orderSummary.customerLastName,
        driverFirstName: orderSummary.driverFirstName,
        driverLastName: orderSummary.driverLastName,
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

  createSubscriptionStrategy({userId, isCompleted, reportId, orderId}, dataSink){
    return new ReportSubscriptionStrategy(this.client, this.getReportContext({userId, reportId, isCompleted, orderId}), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || !isEqual(previousOptions, newOptions);
  }

  transformOptions(options){
    if (typeof options.reportId === 'undefined' || (options.reportId !== 'customerOrderSummary' && options.reportId !== 'driverOrderSummary')){
      throw new Error('reportId should be defined and be either customerOrderSummary or driverOrderSummary');
    }
    if (typeof options.isCompleted === 'undefined'){
      throw new Error('isCompleted  should be defined');
    }
    return options;
  }
}
