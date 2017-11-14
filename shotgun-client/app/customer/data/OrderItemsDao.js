import * as FieldMappings from '../../common/constants/TableNames';
import DispatchingDataSink from '../../common/dataSinks/DispatchingDataSink';
import * as constants from '../../redux/ActionConstants';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';

export default class OrderItemsDao extends DispatchingDataSink{
    static DEFAULT_OPTIONS = (customerId, orderId) =>  {
      return {
        offset: 0,
        limit: 20,
        columnName: undefined,
        columnsToSort: undefined,
        filterMode: 2, //Filtering
        filterExpression: `customerId == "${customerId}" && orderId == "${orderId}"`,
        flags: undefined
      };
    };
  
    constructor(viewserverClient, dispatch, customerId, orderId){
      super();
      this.dispatch = dispatch;
      this.customerId = customerId;
      this.orderId = orderId;
      this.subscriptionStrategy = new DataSourceSubscriptionStrategy(viewserverClient, FieldMappings.ORDER_ITEM_TABLE_NAME);
    }

  subscribe(){
    this.subscriptionStrategy.subscribe(this, OrderItemsDao.DEFAULT_OPTIONS(this.customerId, this.orderId));
  }


  dispatchUpdate(){
      this.dispatch({type: constants.UPDATE_CUSTOMER, customer: {orderDetail: {items: this.rows}}});
    }
}
