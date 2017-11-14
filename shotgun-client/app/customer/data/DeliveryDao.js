import * as FieldMappings from '../../common/constants/TableNames';
import DataSink from '../../common/dataSinks/DataSink';
import * as constants from '../../redux/ActionConstants';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from '../../viewserver-client/Logger';
import uuidv4 from 'uuid/v4';
import moment from 'moment';

export default class DeliveryDao extends DataSink(null) {
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

  createDelivery(){
      return async (dispatch, getState) => {
        //create order object
        const created = moment().format('x');
        const deliveryId = uuidv4();
        dispatch({type: constants.UPDATE_DELIVERY, delivery: {deliveryId, created, lastModified: created}});

        Logger.info(`Creating delivery ${deliveryId}`);
        const addDeliveryRowEvent = this.createAddOrderRowEvent(getState().CheckoutReducer.delivery);
        await this.subscriptionStrategy.editTable(this, [addDeliveryRowEvent]);
        Logger.info('Delivery created');
        return deliveryId;
      };
  }

  createAddOrderRowEvent(delivery){
    return {
      type: 0, // ADD
      columnValues: {
        ...delivery
      }
    };
  }
}
