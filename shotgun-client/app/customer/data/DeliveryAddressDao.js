import * as FieldMappings from '../../common/constants/TableNames';
import * as constants from '../../redux/ActionConstants';
import DispatchingDataSink from '../../common/dataSinks/DispatchingDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';

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

  //TODO - functionality to add and remove cards
}
