import Logger from '../viewserver-client/Logger';

export default DataSink = (superclass) => class extends superclass {  

    constructor(){
        super();
        this.schema = {};
        this.onSnapshotComplete = this.onSnapshotComplete.bind(this);
        this.onDataReset = this.onDataReset.bind(this);
        this.onTotalRowCount = this.onTotalRowCount.bind(this);
        this.onSchemaReset = this.onSchemaReset.bind(this);
        this.onRowAdded = this.onRowAdded.bind(this);
        this.onRowUpdated = this.onRowUpdated.bind(this);
        this.onRowRemoved = this.onRowRemoved.bind(this);
        this.onColumnAdded = this.onColumnAdded.bind(this);
        this.idIndexes = {};
        this.idRows = {};
        this.rows = [];
        this.dirtyRows = [];
    }

    onSnapshotComplete(){
    }

    onDataReset(){
        this.rows = [];
        this.idIndexes = {};
        this.idRows = {};
    }
    onTotalRowCount(count){
        this.totalRowCount = count;
    }
    onSchemaReset(){
        this.schema = {};
    }
    onRowAdded(rowId, row){
        row.key = rowId;
        this.idIndexes[rowId] = this.rows.length;
        this.idRows[rowId] = row;
        this.rows.push(row)
        this.dirtyRows.push(rowId);
        Logger.info("Row added - " + JSON.stringify(row));
    }
    onRowUpdated(rowId, row){
        const rowIndex = this._getRowIndex(rowId);
        this.rows[rowIndex] = row;
        Logger.info("Row updated - " + row);
    }
    onRowRemoved(rowId){
        const rowIndex = this._getRowIndex(rowId);
        this.rows[rowIndex] = row;
    }
    onColumnAdded(colId, col){
       Logger.info("column added - " + JSON.stringify(col));
       this.schema[colId] = col;
    }
    onColumnRemoved(colId){
        delete this.schema[colId];
    }

    getColumn(columnid){
        return this.schema[columnid]
    }
    _getRowIndex(rowId){
        const rowIndex = this.idIndexes[rowId];
        if(typeof rowIndex === 'undefined'){
            throw new Error("Attempting to remove a row that doesn't exist " + rowId + " with " + JSON.stringify(row))
        }
        return rowIndex;
    }
};