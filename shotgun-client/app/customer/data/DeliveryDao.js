import * as FieldMappings from 'common/constants/TableNames';
import RxDataSink from 'common/dataSinks/RxDataSink';
import Logger from 'common/Logger';
import DataSourceSubscriptionStrategy from 'common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import uuidv4 from 'uuid/v4';
import moment from 'moment';

export const createAddDeliveryRowEvent = (delivery) => {
  return {
    type: 0, // ADD
    columnValues: {
      ...delivery
    }
  };
};
export default class DeliveryDaoContext{
  constructor(client, options = {}) {
    this.client = client;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
      return 'delivery';
  }

  createDataSink(){
      return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      delivery: dataSink.rows
    };
  }

  createSubscriptionStrategy(options, dataSink){
    return new DataSourceSubscriptionStrategy(this.client, FieldMappings.DELIVERY_TABLE_NAME, dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions;
  }

  transformOptions(options){
    return options;
  }

  extendDao(dao){
    dao.createDelivery = async () =>{
          //create order object
        const created = moment().format('x');
        const {dataSink} = dao;
        const deliveryId = uuidv4();
        const delivery =  {deliveryId, created, lastModified: created};
        Logger.info(`Creating delivery ${deliveryId}`);
        const addDeliveryRowEvent = createAddDeliveryRowEvent(delivery);
        await dao.subscriptionStrategy.editTable(dataSink, [addDeliveryRowEvent]);
        await dao.rowEventObservable.filter(row => row.deliveryId == deliveryId).timeout(5000, 'Could not detect delivery created in 5 seconds').toPromise();
        Logger.info('Delivery created');
        return deliveryId;
    };
  }
}

