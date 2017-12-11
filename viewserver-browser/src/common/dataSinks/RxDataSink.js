import {Rx} from 'common/rx';
import DataSink from './DataSink';
import * as RxConstants from 'common/rx';

export default class RxDataSink extends DataSink{


  constructor(){
    super();
    this._dataSinkUpdated = new Rx.Subject();
    this.hasSchemaLoaded = false;
  }

  get dataSinkUpdated(){
    return this._dataSinkUpdated;
  }

  onSnapshotComplete(){
    super.onSnapshotComplete();
    this.hasSchemaLoaded = true;
    this._dataSinkUpdated.next({Type: RxConstants.SNAPSHOT_COMPLETE});
  }
  onDataReset(){
    super.onDataReset();
    this._dataSinkUpdated.next({Type: RxConstants.DATA_RESET});
  }
  onTotalRowCount(count){
    super.onTotalRowCount(count);
    this._dataSinkUpdated.next({Type: RxConstants.TOTAL_ROW_COUNT, count});
  }
  onSchemaReset(){
    super.onSchemaReset();
    this.hasSchemaLoaded = false;
    this._dataSinkUpdated.next({Type: RxConstants.SCHEMA_RESET});
  }

  async waitForSchema(){
    if (this.hasSchemaLoaded){
      return Promise.resolve(this.schema);
    }
    await this._dataSinkUpdated.waitForSnapshotComplete().toPromise();
    return Promise.resolve(this.schema);
  }

  onRowAdded(rowId, row){
    super.onRowAdded(rowId, row);
    row.rowId = rowId;
    this._dataSinkUpdated.next({Type: RxConstants.ROW_ADDED, row, rowId});
  }
  onRowUpdated(rowId, row){
    super.onRowUpdated(rowId, row);
    row.rowId = rowId;
    this._dataSinkUpdated.next({Type: RxConstants.ROW_UPDATED, rowId, row: this.rows[rowId]});
  }
  onRowRemoved(rowId){
    const rowToRemove = this.rows[rowId];
    super.onRowRemoved(rowId);
    this._dataSinkUpdated.next({Type: RxConstants.ROW_REMOVED, rowId, row: rowToRemove});
  }
  onColumnAdded(colId, col){
    super.onColumnAdded(colId, col);
    col.colId = colId;
    this._dataSinkUpdated.next({Type: RxConstants.COLUMN_ADDED, col});
  }
  onColumnRemoved(col){
    super.onColumnRemoved(col);
    this._dataSinkUpdated.next({Type: RxConstants.COLUMN_REMOVED, col});
  }
  onError(error){
    super.onError(error);
    this._dataSinkUpdated.next({Type: RxConstants.ERROR, error});
  }
  onSuccess(commandResultId, message){
    super.onSuccess(commandResultId, message);
    this._dataSinkUpdated.next({Type: RxConstants.SUCCESS, commandResultId, message});
  }
}
