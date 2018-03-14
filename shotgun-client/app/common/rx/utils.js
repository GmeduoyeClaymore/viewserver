import Rx from 'rxjs/Rx';
import RxDataSink from 'common/dataSinks/RxDataSink';

const DOMAIN_EVENT_TYPES = [RxDataSink.ROW_ADDED, RxDataSink.ROW_UPDATED, RxDataSink.ROW_REMOVED, RxDataSink.DATA_RESET, RxDataSink.SNAPSHOT_COMPLETE, RxDataSink.DATA_ERROR, RxDataSink.DATA_CLEARED,  RxDataSink.SCHEMA_ERROR, RxDataSink.SCHEMA_ERROR_CLEARED, RxDataSink.CONFIG_ERROR, RxDataSink.CONFIG_ERROR_CLEARED ];

Rx.Observable.prototype.timeoutWithError = function (timeout, error) {
  return this.timeoutWith(timeout, Rx.Observable.throw(error));
};

const checkForErrorEventTypes = (ev) => {
  if (ev.Type ===  RxDataSink.DATA_ERROR){
    throw new Error('Subscription failed as there is a data error on the server side');
  }
  if (ev.Type ===  RxDataSink.SCHEMA_ERROR){
    throw new Error('Subscription failed as there is a schema error on the server side');
  }
  if (ev.Type ===  RxDataSink.CONFIG_ERROR){
    throw new Error('Subscription failed as there is a config error on the server side');
  }
  return ev;
};

Rx.Observable.prototype.waitForSnapshotComplete = function (timeout = 10000) {
  return this.filter(ev => !!~[RxDataSink.SNAPSHOT_COMPLETE, RxDataSink.DATA_ERROR, RxDataSink.SCHEMA_ERROR,  RxDataSink.CONFIG_ERROR].indexOf(ev.Type))
    .take(1)
    .map(c => checkForErrorEventTypes(c))
    .timeoutWithError(timeout, new Error(`No snapshot complete event detected ${timeout} millis seconds after update`));
};

Rx.Observable.prototype.filterRowEvents = function () {
  return this.filter(ev => !!~DOMAIN_EVENT_TYPES.indexOf(ev.Type));
};

Promise.prototype.timeoutWithError = function(timeout, error){
  return Rx.Observable.fromPromise(this).take(1).timeoutWithError(timeout, error).toPromise();
};
