import Rx from 'rxjs/Rx';
import RxDataSink from 'common/dataSinks/RxDataSink';

const DOMAIN_EVENT_TYPES = [RxDataSink.ROW_ADDED, RxDataSink.ROW_UPDATED, RxDataSink.ROW_REMOVED, RxDataSink.DATA_RESET, RxDataSink.SNAPSHOT_COMPLETE];

Rx.Observable.prototype.timeoutWithError = function (timeout, error) {
  return this.timeoutWith(timeout, Rx.Observable.throw(error));
};

Rx.Observable.prototype.waitForSnapshotComplete = function (timeout = 10000) {
  return this.filter(ev => RxDataSink.SNAPSHOT_COMPLETE === ev.Type)
    .take(1)
    .timeoutWithError(timeout, new Error(`No snapshot complete event detected ${timeout} millis seconds after update`));
};

Rx.Observable.prototype.filterRowEvents = function () {
  return this.filter(ev => !!~DOMAIN_EVENT_TYPES.indexOf(ev.Type));
};

Promise.prototype.timeoutWithError = function(timeout, error){
  return Rx.Observable.fromPromise(this).take(1).timeoutWithError(timeout, error).toPromise();
};
