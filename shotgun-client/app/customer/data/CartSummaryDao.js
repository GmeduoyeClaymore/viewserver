import * as constants from '../../redux/ActionConstants';
import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';
import DispatchingDataSink from '../../common/dataSinks/DispatchingDataSink';


export default class CartSummaryDao extends DispatchingDataSink{
  constructor(viewserverClient, customerId, dispatch){
    super();
    this.viewserverClient = viewserverClient;
    this.customerId = customerId;
    this.dispatch = dispatch;

    const reportContext = {
      reportId: 'cartSummary',
      parameters: {
        customerId
      }
    };
    this.subscriptionStrategy = new ReportSubscriptionStrategy(viewserverClient, reportContext);
    this.subscriptionStrategy.subscribe(this, {limit: 1});
  }

  dispatchUpdate(){
    this.dispatch({type: constants.UPDATE_CART, cart: this.mapCartSummary()});
  }

  mapCartSummary(){
    const summaryRow = this.rows[0];
    return summaryRow !== undefined ? {totalPrice: summaryRow.totalPrice, totalQuantity: summaryRow.totalQuantity} : {totalPrice: 0, totalQuantity: 0};
  }
}
