import Rx from 'rx-lite';

export default class CoolRxDataSink{
  constructor(){
    this._rowAdded = new Rx.ReplaySubject(1);
    this._snapshotComplete = new Rx.ReplaySubject(1);
    this._onDataReset = new Rx.ReplaySubject(1);
    this._onTotalRowCount = new Rx.ReplaySubject(1);
    this._onSchemaReset = new Rx.ReplaySubject(1);
    this._onRowAdded = new Rx.ReplaySubject(1);
    this._onRowUpdated = new Rx.ReplaySubject(1);
    this._onRowRemoved = new Rx.ReplaySubject(1);
    this._onColumnAdded = new Rx.ReplaySubject(1);
  }
  get rowAddedObservable(){
    return this._rowAdded;
  }
  get onSnapshotCompleteObservable(){
    return this._snapshotComplete;
  }
  get onDataResetObservable(){
    return this._onDataReset;
  }
  get onTotalRowCountObservable(){
    return this._onTotalRowCount;
  }
  get onSchemaResetObservable(){
    return this._onSchemaReset;
  }
  get onRowAddedObservable(){
    return this._onRowAdded;
  }
  get onRowUpdatedObservable(){
    return this._onRowUpdated;
  }
  get onRowRemovedObservable(){
    return this._onRowRemoved;
  }
  get onColumnAddedObservable(){
    return this._onColumnAdded;
  }
  onSnapshotComplete(){
    this._snapshotComplete.onNext(this.rows);
  }
  onDataReset(){
    this._onDataReset.onNext();
  }
  onTotalRowCount(count){
    this._onTotalRowCount.onNext(count);
  }
  onSchemaReset(){
    this._onSchemaReset.onNext();
  }
  onRowAdded(rowId, row){
    row.rowId = rowId;
    this._onRowAdded.onNext(row);
  }
  onRowUpdated(rowId, row){
    row.rowId = rowId;
    this._onRowUpdated.onNext(row);
  }
  onRowRemoved(rowId){
    this._onRowRemoved.onNext(rowId);
  }
  onColumnAdded(colId, col){
    col.colId = colId;
    this._onColumnAdded.onNext(col);
  }
  onColumnRemoved(col){
    this._onColumnRemoved.onNext(col);
  }
}
