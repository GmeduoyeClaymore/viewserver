import Logger from '../viewserver-client/Logger';

export default DataSink = (superclass) => class extends superclass {  

    constructor(){
        super();
        this.schema = {};
        this.columnsByName = {};
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
        if(super.onSnapshotComplete){
            super.onSnapshotComplete()
        }
    }

    onDataReset(){
        if(super.onDataReset){
            super.onDataReset()
        }
        this.rows = [];
        this.idIndexes = {};
        this.idRows = {};
    }
    onTotalRowCount(count){
        if(super.onTotalRowCount){
            super.onTotalRowCount(count)
        }
        this.totalRowCount = count;
        Logger.info("Total row count is - " + this.totalRowCount);
    }
    onSchemaReset(){
        if(super.onSchemaReset){
            super.onSchemaReset()
        }
        this.schema = {};
    }
    onRowAdded(rowId, row){
        if(super.onRowAdded){
            super.onRowAdded(rowId, row)
        }
        row.key = rowId;
        this.idIndexes[rowId] = this.rows.length;
        this.idRows[rowId] = row;
        this.rows.push(row)
        this.dirtyRows.push(rowId);
        Logger.info(`Row added - ${rowId} -  + ${JSON.stringify(row)}`);
    }
    onRowUpdated(rowId, row){
        if(super.onRowUpdated){
            super.onRowUpdated(rowId, row)
        }
        const rowIndex = this._getRowIndex(rowId);
        this.rows[rowIndex] = row;
        Logger.info("Row updated - " + row);
    }
    onRowRemoved(rowId){
        if(super.onRowRemoved){
            super.onRowRemoved(rowId)
        }
        const rowIndex = this._getRowIndex(rowId);
        this.rows[rowIndex] = row;
    }
    onColumnAdded(colId, col){
       Logger.info(`column added - ${colId} -  + ${JSON.stringify(col)}`);
       if(super.onColumnAdded){
            super.onColumnAdded(colId, col)
       }
       const newCol = {...col,colId}
       this.schema[colId] = newCol;
       this.columnsByName[col.name] = newCol;;
    }
    onColumnRemoved(colId){
        if(super.onColumnRemoved){
            super.onColumnRemoved(colId)
        }
        var col = this.schema[colId];
        if(col){
            delete this.columnsByName[col.name];
            delete this.schema[colId];
        }
    }

    getColumn(columnid){
        return this.schema[columnid]
    }

    getColumnId(name){
        const result = this.columnsByName[name];
        return result ? result.colId : undefined;
    }

    _getRowIndex(rowId){
        const rowIndex = this.idIndexes[rowId];
        if(typeof rowIndex === 'undefined'){
            throw new Error("Attempting to remove a row that doesn't exist " + rowId + " with " + JSON.stringify(row))
        }
        return rowIndex;
    }
};