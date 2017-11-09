import * as FieldMappings from './FieldMappings';
import DataSink from '../../common/dataSinks/DataSink';
import CoolRxDataSink from '../../common/dataSinks/CoolRxDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from '../../viewserver-client/Logger';

import uuidv4 from 'uuid/v4';
import moment from 'moment';

export default class DeliveryDao extends DataSink(CoolRxDataSink) {
  static DEFAULT_OPTIONS = () =>  {
    return {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      flags: undefined
    };
  };

  constructor(viewserverClient) {
    super();
    //TODO - it's crazy we need to subscribe just to be able to update a thing....
    this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.DELIVERY_TABLE_NAME);
    this.subscriptionStrategy.subscribe(this, DeliveryDao.DEFAULT_OPTIONS());
  }

  async createDelivery(delivery){
    //create order object
    const deliveryId = uuidv4();
    Logger.info(`Creating delivery ${deliveryId}`);
    const addOrderRowEvent = this.createAddOrderRowEvent(deliveryId, delivery);
    await this.subscriptionStrategy.editTable(this, [addOrderRowEvent]);
    Logger.info('Create delivery promise resolved');
    return deliveryId;
  }

  createAddOrderRowEvent(deliveryId, delivery){
    const now = moment();
    delivery.dueTime = now.add(delivery.eta, 'hours').format('x');
    delete delivery.eta;
    const created = now.format('x');

    return {
      type: 0, // ADD
      columnValues: {
        deliveryId,
        created,
        lastModified: created,
        ...delivery
      }
    };
  }
}
