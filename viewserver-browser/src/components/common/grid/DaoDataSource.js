import {Rx} from 'common/rx';

export default class DaoDataSource{
    constructor(dao){
        this.dao = dao;
        this.dataSink = dao.dataSink;
        this.view = {}
        this.view.request = this.handleDataRequest.bind(this);
        this.dataRequestedSubject = new Rx.Subject();
        this.onResized = new Rx.Subject();
        this.onChanged = new Rx.Subject();
        this.columnsChanged = new Rx.Subject();
    }

  
    handleDataSinkUpdate(evt){
        switch(evt.Type) {
            case RxDataSink.SNAPSHOT_COMPLETE:
                this.onChanged(this.pendingRequest)
                this.pendingSnapshotComplete = false;
                this.pendingRequest = undefined;
                break;
            case RxDataSink.DATA_RESET:
                this.onChanged({rowStart : 0, rowEnd : this.dataSink.rows.size,colStart : undefined,colEnd : undefined})
                break;
            case RxDataSink.TOTAL_ROW_COUNT:
                this.onResized.next();
                break;
            case RxDataSink.SCHEMA_RESET:
                break;
            case RxDataSink.ROW_ADDED:
            case RxDataSink.ROW_UPDATED:
            case RxDataSink.ROW_REMOVED:
                if(!this.pendingSnapshotComplete){
                    const rowIndex = this.dataSink._getRowIndex(evt.rowId);
                    this.onChanged({rowStart : rowIndex ,rowEnd : rowIndex  + 1,colStart : undefined,colEnd : undefined})
                }
            case RxDataSink.COLUMN_REMOVED:
            case RxDataSink.COLUMN_ADDED:
                this.columnsChanged.next();
                break;
            case RxDataSink.ERROR:
            case RxDataSink.SUCCESS:
                break;
        }
    }

    handleDataRequest(rowStart,rowEnd,colStart,colEnd){
        this.pendingSnapshotComplete = true;
        this.pendingRequest = {rowStart,rowEnd,colStart,colEnd};
        this.dao.updateSubscription({offset : rowStart, limit : rowEnd});
    }

    get(index){
        return this.dataSink.rows[index];
    }

    get size(){
        return this.dataSink.totalRowCount;
    }

    get columns(){
        return this.dataSink.schema.columns;
    }

    getKey(row){
        return row.rowId;
    }
}