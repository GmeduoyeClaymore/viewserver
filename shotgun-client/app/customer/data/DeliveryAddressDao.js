import RxDataSink from '../../common/dataSinks/RxDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';

const createAddDeliveryAddressEvent = (deliveryAddress) =>{
  return {
    type: 0, // ADD
    columnValues: {
      ...deliveryAddress
    }
  };
};

const createUpdateDeliveryAddressEvent = (deliveryAddress) => {
  return {
    type: 1, // UPDATE
    columnValues: {
      ...deliveryAddress
    }
  };
};

export default class OrderItemsDaoContext{
  constructor(client, options) {
    this.client = client;
    this.options = options;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 100,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  }

  get name(){
      return 'orderItems';
  }

  createDataSink(){
      return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return {
      customer: {
        deliveryAddresses: dataSink.rows
      }
    };
  }

  createSubscriptionStrategy(){
    return new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.DELIVERY_ADDRESS_TABLE_NAME);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.customerId != newOptions.customerId;
  }

  transformOptions(options){
    const {customerId} = options;
    if (typeof customerId === 'undefined'){
      throw new Error('customerId should be defined');
    }
    return {...options, filterExpression: `customerId == "${customerId}"`};
  }

  extendDao(dao){
    dao.addOrUpdateDeliveryAddress = async ({customerId, deliveryAddress}) => {
      const {dataSink, subscriptionStrategy} = dao;
      const {schema} = dataSink;
      const customer = dataSink.rows[0];
      let deliveryAddressRowEvent;
      Logger.info(`Adding deliveryAddress schema is ${JSON.stringify(schema)}`);
      const deliveryAddressObject = {};
      forEach(schema, value => {
        const field = value.name;
        deliveryAddressObject[field] = deliveryAddress[field];
      });
  
      deliveryAddressObject.customerId = customerId;
  
      if (deliveryAddressObject.deliveryAddressId == undefined) {
        deliveryAddressObject.deliveryAddressId = uuidv4();
      }
  
      if (customer == undefined){
        Logger.info(`Adding deliveryAddress ${JSON.stringify(deliveryAddressObject)}`);
        deliveryAddressRowEvent = createAddDeliveryAddressEvent(deliveryAddressObject);
      } else {
        Logger.info(`Updating deliveryAddress ${JSON.stringify(deliveryAddressObject)}`);
        deliveryAddressRowEvent = createUpdateDeliveryAddressEvent(deliveryAddressObject);
      }
      const modifiedRows = await subscriptionStrategy.editTable(dataSink, [deliveryAddressRowEvent]);
      Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
      await dao.rowEventObservable.filter(row => row.deliveryAddressId == deliveryAddressRowEvent.columnValues.deliveryAddressId).timeout(5000, 'Could not detect created delivery address in 5 seconds').toPromise();
      return modifiedRows;
    };
  }
}
