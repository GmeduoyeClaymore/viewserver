import DataSink from '../../common/dataSinks/DataSink';
import CoolRxDataSink from '../../common/dataSinks/CoolRxDataSink';
import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';
import Rx from 'rx-lite';

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
    this.data = Rx.Observable.merge(this.onRowAddedObservable, this.onRowUpdatedObservable, this.onRowRemovedObservable);
    this.subscriptionStrategy.subscribe(this, {limit: 1});
  }

  subscribe(func){
    this.data.subscribe(func);
  }
}
