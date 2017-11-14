import * as constants from '../../redux/ActionConstants';
import DispatchingDataSink from '../../common/dataSinks/DispatchingDataSink';
import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';
import Logger from '../../viewserver-client/Logger';

//TODO - fix table edit so you don't have to subscribe and then merge this with OrderDao
export default class OrderSummaryDao extends DispatchingDataSink {
  static OPTIONS = {
    offset: 0,
    limit: 10,
    columnsToSort: [{name: 'created', direction: 'desc', limit: 100}]
  };

  constructor(client, dispatch, customerId, isCompleted, orderId) {
    super();
    this.dispatch = dispatch;
    this.isCompleted = isCompleted;
    this.subscriptionStrategy = new ReportSubscriptionStrategy(client, this.getReportContext(customerId, isCompleted, orderId));
  }

  getReportContext(customerId, isCompleted, orderId){
    return {
      reportId: 'orderSummary',
      parameters: {
        customerId,
        isCompleted,
        orderId
      }
    };
  }

  subscribe(){
    this.dispatch({type: constants.UPDATE_CUSTOMER,  customer: {status: {busy: true}}});
    this.subscriptionStrategy.subscribe(this, OrderSummaryDao.OPTIONS);
  }

  onSnapshotComplete(){
    super.onSnapshotComplete();
    this.dispatch({type: constants.UPDATE_CUSTOMER, customer: {status: {busy: false}}});
  }

  dispatchUpdate(){
    let orderType = 'complete';

    if (!this.isCompleted) {
      orderType = 'incomplete';
    }

    this.dispatch({type: constants.UPDATE_CUSTOMER, customer: {orders: {[orderType]: this.rows}}});
  }

  page(offset, limit){
    if (this.rows.length >= this.totalRowCount){
      Logger.info('Reached end of viewport');
      return false;
    }

    Logger.info(`Paging: offset ${offset} limit ${limit}`);

    const newOptions = Object.assign({}, OrderSummaryDao.OPTIONS, {offset, limit});
    this.subscriptionStrategy.update(this, newOptions);
    this.dispatch({type: constants.UPDATE_CUSTOMER, customer: {status: {busy: true}}});
    return true;
  }
}
