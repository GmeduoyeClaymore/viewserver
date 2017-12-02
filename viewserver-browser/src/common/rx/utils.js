const Rx =  require('rxjs/Rx');
import RxDataSink from 'common/dataSinks/RxDataSink';

const DOMAIN_EVENT_TYPES = [RxDataSink.ROW_ADDED, RxDataSink.ROW_UPDATED, RxDataSink.ROW_REMOVED, RxDataSink.DATA_RESET];

Rx.Observable.prototype.timeoutWithError = function (timeout, error) {
  return this.timeoutWith(timeout, Rx.Observable.throw(error));
};

Rx.Observable.prototype.waitForSnapshotComplete = function (timeout = 10000) {
  return this.filter(ev => RxDataSink.SNAPSHOT_COMPLETE === ev.Type)
    .take(1)
    .timeoutWithError(timeout, new Error('No snapshot complete event detected 10 seconds after update'));
};

Rx.Observable.prototype.filterRowEvents = function () {
  return this.filter(ev => !!~DOMAIN_EVENT_TYPES.indexOf(ev.Type));
};
