import Rx from 'rx-lite';
import DataSink from './DataSink';

export default class RxDataSink extends DataSink(null){
  static SNAPSHOT_COMPLETE = 'SnapshotComplete';
  static DATA_RESET = 'SnapshotComplete';
  static TOTAL_ROW_COUNT = 'TotalRowCount';
  static SCHEMA_RESET = 'SchemaReset';
  static ROW_ADDED = 'RowAdded';
  static ROW_UPDATED = 'RowUpdated';
  static ROW_REMOVED = 'RowRemoved';
  static COLUMN_ADDED = 'ColumnAdded';
  static COLUMN_REMOVED = 'ColumnRemoved';
  static ERROR = 'Error';
  static SUCCESS = 'Success';

  constructor(){
    super();
    this._dataSinkUpdated = new Rx.Subject();
  }

  get dataSinkUpdated(){
    return this._dataSinkUpdated;
  }

  onSnapshotComplete(){
    super.onSnapshotComplete();
    this._dataSinkUpdated.onNext({Type: RxDataSink.SNAPSHOT_COMPLETE});
  }
  onDataReset(){
    super.onDataReset();
    this._dataSinkUpdated.onNext({Type: RxDataSink.DATA_RESET});
  }
  onTotalRowCount(count){
    super.onTotalRowCount(count);
    this._dataSinkUpdated.onNext({Type: RxDataSink.TOTAL_ROW_COUNT, count});
  }
  onSchemaReset(){
    super.onSchemaReset();
    this._dataSinkUpdated.onNext({Type: RxDataSink.SCHEMA_RESET});
  }
  onRowAdded(rowId, row){
    super.onRowAdded(rowId, row);
    row.rowId = rowId;
    this._dataSinkUpdated.onNext({Type: RxDataSink.ROW_ADDED, row});
  }
  onRowUpdated(rowId, row){
    super.onRowUpdated(rowId, row);
    row.rowId = rowId;
    this._dataSinkUpdated.onNext({Type: RxDataSink.ROW_UPDATED, row});
  }
  onRowRemoved(rowId){
    super.onRowRemoved(rowId);
    this._dataSinkUpdated.onNext({Type: RxDataSink.ROW_REMOVED, rowId});
  }
  onColumnAdded(colId, col){
    super.onColumnAdded(colId, col);
    col.colId = colId;
    this._dataSinkUpdated.onNext({Type: RxDataSink.COLUMN_ADDED, col});
  }
  onColumnRemoved(col){
    super.onColumnRemoved(col);
    this._dataSinkUpdated.onNext({Type: RxDataSink.COLUMN_REMOVED, col});
  }
  onError(error){
    super.onError(error);
    this._dataSinkUpdated.onNext({Type: RxDataSink.ERROR, error});
  }
  onSuccess(commandResultId, message){
    super.onSuccess(commandResultId, message);
    this._dataSinkUpdated.onNext({Type: RxDataSink.SUCCESS, commandResultId, message});
  }
}
