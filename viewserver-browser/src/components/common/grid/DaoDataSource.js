import {Rx} from 'common/rx';
import RxDataSink from 'common/dataSinks/RxDataSink';
import Logger from 'common/Logger';

export default class DaoDataSource{
    constructor(dao){
        this.dao = dao;
        this.view = {}
        this.view.request = this.handleDataRequest.bind(this);
        this.dataRequestedSubject = new Rx.Subject();
        this.onResized = new Rx.Subject();
        this.onChanged = new Rx.Subject();
        this.columnsChanged = new Rx.Subject();
        this.dao.rawDataObservable.subscribe(this.handleDataSinkUpdate.bind(this));
    }
  
    handleDataSinkUpdate(evt){
        switch(evt.Type) {
            case RxDataSink.SNAPSHOT_COMPLETE:
                this.onChanged.next(this.getPendingRequest())
                this.pendingSnapshotComplete = false;
                this.pendingRequest = undefined;
                break;
            case RxDataSink.DATA_RESET:
                this.onChanged.next({rowStart : 0, rowEnd : this.dao.dataSink.rows.length,colStart : undefined,colEnd : undefined})
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
                    const rowIndex = this.dao.dataSink._getRowIndex(evt.rowId);
                    this.onChanged.next({rowStart : rowIndex ,rowEnd : rowIndex  + 1,colStart : undefined,colEnd : undefined})
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

    getPendingRequest(){
        if(this.pendingRequest){
            return this.pendingRequest;
        }
        if(!this.dao.options){
            throw new Error("not sure how you can have a snapshot complete with no options on the dao")
        }
        return this.createPendingRequestFromOptions(this.dao.options);
    }

    createPendingRequestFromOptions({offset,limit}){
        return {rowStart : offset, rowEnd : limit};
    }

    async handleDataRequest(rowStart,rowEnd,colStart,colEnd){
        this.pendingSnapshotComplete = true;
        this.pendingRequest = {rowStart,rowEnd,colStart,colEnd};
        try{
            await this.dao.updateSubscription({offset : rowStart, limit : rowEnd});
        }catch(exception){
            Logger.warning(`Issue updating subscription ${exception}. Options have been updated though.`)
        }
    }

    get dataSink(){
        return this.dao.dataSink || {};
    }

    get(index){
        return this.dataSink.rows[index];
    }

    get size(){
        return this.dataSink.totalRowCount;
    }

    get columns(){
        return this.dataSink.schema ? this.mapColumns(this.dataSink.schema) : [];
    }

    mapColumns(cols = {}){
        const result = [];
        Object.values(cols).forEach(
            col => result.push({...col,key:col.name, title: col.name, width : 100})
        );
        return result;
    }

    getKey(row){
        return row ? row.rowId : undefined;
    }
}