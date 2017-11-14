import * as FieldMappings from '../../common/constants/TableNames';
import * as constants from '../../redux/ActionConstants';
import DataSink from '../../common/dataSinks/DataSink';
import Logger from '../../viewserver-client/Logger';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import uuidv4 from 'uuid/v4';
import moment from 'moment';

export default class OrderDao extends DataSink(null){
    static DEFAULT_OPTIONS = (customerId) =>  {
      return {
        offset: 0,
        limit: 20,
        columnName: undefined,
        columnsToSort: undefined,
        filterMode: 2, //Filtering
        filterExpression: `customerId == "${customerId}"`,
        flags: undefined
      };
    };
  
    constructor(viewserverClient, customerId){
      super();
      this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.ORDER_TABLE_NAME);
      this.viewserverClient = viewserverClient;
      this.customerId = customerId;
      this.subscriptionStrategy.subscribe(this, OrderDao.DEFAULT_OPTIONS(this.customerId));
    }
  
    createOrder(){
      return async (dispatch, getState) => {
        const orderId = uuidv4();
        const created = moment().format('x');
        Logger.info(`Creating order ${orderId}`);
        dispatch({type: constants.UPDATE_ORDER, order: {orderId, created, lastModified: created, customerId: this.customerId}});

        const addOrderRowEvent = this.createAddOrderRowEvent(getState().CheckoutReducer.order);
        await this.subscriptionStrategy.editTable(this, [addOrderRowEvent]);
        Logger.info('Order created');
        return orderId;
      };
    }

    createAddOrderRowEvent(order){
      return {
        type: 0, // ADD
        columnValues: {
          ...order
        }
      };
    }
}
