import React, {Component,PropTypes} from 'react';
import {View, ListView, ScrollView, StyleSheet, Text, Dimensions } from 'react-native';
import Logger from '../viewserver-client/Logger';
import InfiniteVirtualizedList from 'react-native-infinite-virtualized-list';

const styles = StyleSheet.create({
    contentContainer: {
        paddingVertical: 20
    }
});

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
        filterMode :  3,//Filtering
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
        this._onScroll = this._onScroll.bind(this)
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

    _onScroll(e){
        var windowHeight = Dimensions.get('window').height
        const {isNextPageLoading} = this.state;
        height = e.nativeEvent.contentSize.height
        offset = e.nativeEvent.contentOffset.y; 
        if( windowHeight + offset >= (height * 0.75) ){ 
            if(isNextPageLoading){
                this.setState({loadingQueued : true});
            }
            else{
                this.loadNextPage();
            }
        }
      }
    
    loadNextPage(){
        if(this.rows.length >= this.totalRowCount){
            Logger.info("Reached end of viewport")
            return;
        }
        const { offset,limit } = this.state.options
        const newLimit = limit + DataSinkListView.DEFAULT_OPTIONS.limit;
        const newOptions = {...this.state.options};
        newOptions.limit = newLimit;
        this.props.subscriptionStrategy.update(this,newOptions);
        this.setState({options : newOptions, isNextPageLoading : true})
    }
    
    search(expression){
        const newOptions = {...this.state.options};
        newOptions.filterExpression = expression;
        this.props.subscriptionStrategy.update(this,newOptions);
        this.setState({options : newOptions})
    }
    
    refresh(){
        this.props.subscriptionStrategy.update(this.props.client,DataSinkListView.DEFAULT_OPTIONS);
        this.setState({options : DataSinkListView.DEFAULT_OPTIONS})
    }

    renderItem = (item) => {
        const RowView = this.props.rowView
        return <RowView key={item["P_ID"]} style={{flex : 1}} item={item}/>
    }
    
    render() {
        const { rows } = this
        const {  hasNextPage, isNextPageLoading, refreshing } = this.state
        const { emptyView, paginationWaitingView, headerView : HeaderView, refreshable, ...otherProps } = this.props
        if (rows.length === 0) return emptyView()
        return (
            <View style={{flex : 1,flexDirection : 'column'}}>
                <View style={{height : 60}}>
                    <HeaderView/>
                </View>
                <ScrollView contentContainerStyle={styles.contentContainer} style={{flex: 1, flexDirection: 'column'}} onScroll={this._onScroll}>
                    {this.rows.map( c => this.renderItem(c))}
                </ScrollView >
            </View>
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
        const {isNextPageLoading,loadingQueued} = this.state;
        const { offset,limit } = this.state.options
        if(this.rows.length >= limit && isNextPageLoading){
            if(loadingQueued){
                this.loadNextPage();
            }
            this.setState({
                isNextPageLoading: false,
                loadingQueued : false
            })
        }
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