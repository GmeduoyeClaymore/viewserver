import DataSink from '../../common/DataSink';
import CoolRxDataSink from '../../common/CoolRxDataSink';
import ReportSubscriptionStrategy from '../../common/ReportSubscriptionStrategy';

export default class CartSummaryDao extends DataSink(CoolRxDataSink){
  constructor(viewserverClient, customerId){
    super();
    this.viewserverClient = viewserverClient;
    this.customerId = customerId;

    const reportContext = {
      reportId: 'cartSummary',
      parameters: {
        customerId
      }
    };
    this.subscriptionStrategy = new ReportSubscriptionStrategy(viewserverClient, reportContext);
    this.totalQuantity = this.onRowAddedOrUpdatedObservable.select(row => row.totalQuantity);
    this.subscriptionStrategy.subscribe(this, {limit: 1});
  }

  get totalQuantityObservable() {
    return this.totalQuantity;
  }
}
