import * as FieldMappings from '../../common/constants/TableNames';
import * as constants from '../../redux/ActionConstants';
import DispatchingDataSink from '../../common/dataSinks/DispatchingDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import uuidv4 from 'uuid/v4';
import Logger from '../../viewserver-client/Logger';
import {forEach} from 'lodash';

export default class DeliveryAddressDao extends DispatchingDataSink {
  static DEFAULT_OPTIONS = (customerId) => {
    return {
      offset: 0,
      limit: 100,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      filterExpression: `customerId == "${customerId}"`,
      flags: undefined
    };
  };

  constructor(viewserverClient, customerId, dispatch) {
    super();
    this.dispatch = dispatch;
    this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.DELIVERY_ADDRESS_TABLE_NAME);
    this.subscriptionStrategy.subscribe(this, DeliveryAddressDao.DEFAULT_OPTIONS(customerId));
  }

  dispatchUpdate(){
    this.dispatch({type: constants.UPDATE_CUSTOMER, customer: {deliveryAddresses: this.rows}});
  }

  async addOrUpdateDeliveryAddress(customerId, deliveryAddress){
    let deliveryAddressRowEvent;

    Logger.info(`Adding deliveryAddress schema is ${JSON.stringify(this.schema)}`);
    const deliveryAddressObject = {}
    forEach(this.schema, value => {
      const field = value.name;
      deliveryAddressObject[field] = deliveryAddress[field];
    });

    deliveryAddressObject.customerId = customerId;

    if (deliveryAddressObject.deliveryAddressId == undefined) {
      deliveryAddressObject.deliveryAddressId = uuidv4();
    }

    if (this.customer == undefined){
      Logger.info(`Adding deliveryAddress ${JSON.stringify(deliveryAddressObject)}`);
      deliveryAddressRowEvent = this.createAddDeliveryAddressEvent(deliveryAddressObject);
    } else {
      Logger.info(`Updating deliveryAddress ${JSON.stringify(deliveryAddressObject)}`);
      deliveryAddressRowEvent = this.createUpdateDeliveryAddressEvent(deliveryAddressObject);
    }
    const modifiedRows = await this.subscriptionStrategy.editTable(this, [deliveryAddressRowEvent]);
    Logger.info(`Add item promise resolved ${JSON.stringify(modifiedRows)}`);
    return modifiedRows;
  }

  createAddDeliveryAddressEvent(deliveryAddress){
    return {
      type: 0, // ADD
      columnValues: {
        ...deliveryAddress
      }
    };
  }

  createUpdateDeliveryAddressEvent(deliveryAddress){
    return {
      type: 1, // UPDATE
      columnValues: {
        ...deliveryAddress
      }
    };
  }

  //TODO - functionality  remove addreses
}
