import React, {Component,PropTypes} from 'react';
import {View, ListView, StyleSheet} from 'react-native';
import Logger from '../viewserver-client/Logger';
import InfiniteVirtualizedList from 'react-native-infinite-virtualized-list';

export default class DataSinkListView extends Component{
    static propTypes = {
        subscriptionStrategy: PropTypes.object.isRequired,
        rowView: PropTypes.func.isRequired,
        paginationWaitingView: PropTypes.func.isRequired,
        emptyView: PropTypes.func.isRequired,
        paginationAllLoadedView: PropTypes.func.isRequired,
        refreshable: PropTypes.bool.isRequired,
        enableEmptySections: PropTypes.bool.isRequired,
        removeRow: PropTypes.func,
    }

    static DEFAULT_OPTIONS = {
        offset :  0,
        limit :  20,
        columnName  :  undefined,
        columnsToSort :  undefined,
        filterMode :  2,//Filtering
        filterExpression : undefined,
        flags : undefined
    }
    

    constructor(props){
        super(props);
        this.schema = {};
        this.onSnapshotComplete = this.onSnapshotComplete.bind(this);
        this.onDataReset = this.onDataReset.bind(this);
        this.onTotalRowCount = this.onTotalRowCount.bind(this);
        this.onSchemaReset = this.onSchemaReset.bind(this);
        this.onRowAdded = this.onRowAdded.bind(this);
        this.onRowUpdated = this.onRowUpdated.bind(this);
        this.onRowRemoved = this.onRowRemoved.bind(this);
        this.onColumnAdded = this.onColumnAdded.bind(this);
        this.loadNextPage = this.loadNextPage.bind(this);
        this.idIndexes = {};
        this.idRows = {};
        this.rows = [];
        this.dirtyRows = [];
    }

 
    state = {
        list: [],
        page: 1,
        hasNextPage : true,
        refreshing: false,
        options : DataSinkListView.DEFAULT_OPTIONS
    }
    
    componentDidMount() {
        this.subscribe();
    }

    subscribe(){
        this.setState({
            isNextPageLoading: true
        });
        this.props.subscriptionStrategy.subscribe(this,this.state.options);
    }
    
    loadNextPage(){
        /*const { offset,limit } = this.state.options
        const newOffset = offset + limit;
        const newLimit = newOffset + DataSinkListView.DEFAULT_OPTIONS.limit;
        const newOptions = {...this.state.options};
        newOptions.offset = newOffset;
        newOptions.limit = newLimit;
        this.props.subscriptionStrategy.update(this,this.state.options);
        this.setState({options : newOptions})*/
        return Promise.resolve();
    }
    
    search(expression){
        newOptions.filterExpression = expression;
        this.props.subscriptionStrategy.update(this,this.state.options);
        this.setState({options : newOptions})
    }
    
    refresh(){
        this.props.subscriptionStrategy.update(this.props.client,DataSinkListView.DEFAULT_OPTIONS);
        this.setState({options : DataSinkListView.DEFAULT_OPTIONS})
    }

    renderItem = ({ item }) => {
        return this.props.rowView(item)
    }
    
    render() {
        const { rows } = this
        const {  hasNextPage, isNextPageLoading, refreshing } = this.state
        const { emptyView, paginationWaitingView, headerView, refreshable, ...otherProps } = this.props

        if (rows.length === 0) return emptyView()
        return (
            <InfiniteVirtualizedList
            data={this.rows}
            hasNextPage={hasNextPage}
            isNextPageLoading={isNextPageLoading}
            loadNextPage={this.loadNextPage}
            renderItem={this.renderItem}
            paginationWaitingView={paginationWaitingView}
            getItem={(data, index) => rows[index]}
            getItemCount={() => rows.length}
            ListHeaderComponent={headerView}
            onRefresh={refreshable ? this.refresh : null}
            refreshing={refreshing}
            {...otherProps}
            />
        )
    }

    onSnapshotComplete(){
        this.setState({
            isNextPageLoading: false
        })
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
       Logger.info("column added - " + col.name);
       this.schema[colId] = col;
    }
    onColumnRemoved(colId){
        delete this.schema[colId];
    }

    _getRowIndex(rowId){
        const rowIndex = this.idIndexes[rowId];
        if(typeof rowIndex === 'undefined'){
            throw new Error("Attempting to remove a row that doesn't exist " + rowId + " with " + JSON.stringify(row))
        }
        return rowIndex;
    }
  }