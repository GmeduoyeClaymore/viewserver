const Rx =  require('rxjs/Rx');
import RxDataSink from 'common/dataSinks/RxDataSink';

export const SNAPSHOT_COMPLETE = 'SnapshotComplete';
export const DATA_RESET = 'DataReset';
export const TOTAL_ROW_COUNT = 'TotalRowCount';
export const SCHEMA_RESET = 'SchemaReset';
export const ROW_ADDED = 'RowAdded';
export const ROW_UPDATED = 'RowUpdated';
export const ROW_REMOVED = 'RowRemoved';
export const COLUMN_ADDED = 'ColumnAdded';
export const COLUMN_REMOVED = 'ColumnRemoved';
export const ERROR = 'Error';
export const SUCCESS = 'Success';

const DOMAIN_EVENT_TYPES = [ROW_ADDED, ROW_UPDATED, ROW_REMOVED, DATA_RESET];

Rx.Observable.prototype.timeoutWithError = function (timeout, error) {
  return this.timeoutWith(timeout, Rx.Observable.throw(error));
};

Rx.Observable.prototype.waitForSnapshotComplete = function (timeout = 10000) {
  return this.filter(ev => SNAPSHOT_COMPLETE === ev.Type)
    .take(1)
    .timeoutWithError(timeout, new Error('No snapshot complete event detected 10 seconds after update'));
};

Rx.Observable.prototype.waitForSuccess = function (timeout = 10000) {
  return this.filter(ev => SUCCESS === ev.Type || ERROR === ev.Type)
    .take(1)
    .timeoutWithError(timeout, new Error('No success or error detected within 10 seconds'))
    .map(ev => {
      if(ev.Type === ERROR){
        throw new Error(ev.error)
      }
      return ev;
    });
};

Rx.Observable.prototype.filterRowEvents = function () {
  return this.filter(ev => !!~DOMAIN_EVENT_TYPES.indexOf(ev.Type));
};
