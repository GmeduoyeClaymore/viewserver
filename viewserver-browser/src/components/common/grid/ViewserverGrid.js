import React, { Component, PropTypes } from 'react';
import Grid from './GridView';
import { isEqual, debounce } from 'lodash';
import DaoDataSource from './DaoDataSource';
import {DAO_REGISTRATION_CONTEXT} from 'custom-redux/DaoMiddleware'
import { renderColumnHeaderContent } from './ViewServerGridColumnHeader';
import GenericOperatorDaoContext from 'dao/GenericOperatorDaoContext';
import Logger from 'common/Logger';
import Dao from 'common/dao/DaoBase';
import { ScaleLoader } from 'react-spinners';
import moment from 'moment';

const CONTAINER_STYLE = { display: 'flex', flexDirection: 'column', flex: '1', overflow: 'hidden' };
const MODAL_STYLE = {
    zIndex: 1, /* Sit on top */
    left: '0',
    top: '0',
    width: '100%', /* Full width */
    height: '100%', /* Full height */
    overflow: 'auto', /* Enable scroll if needed */
    backgroundColor: 'rgb(0,0,0)', /* Fallback color */
    backgroundColor: 'rgba(255,255,255,0.4)' /* Black w/ opacity */
}
const ROW_HEIGHT = 12;

export default class ViewServerGrid extends Component {
    static propTypes = {
        title: PropTypes.string,
        daoName: PropTypes.string.isRequired,
    };

    static defaultProps = {
        disableInlineFilters: true
    };

    static contextTypes = {
        userHeight: PropTypes.number
    };

    constructor(props, context) {
        super(props, context);
        const state = {
            columns: this.dataSource && this.dataSource.columns || [],
            inlineFiltersVisible: false
        };

        const { settings, disableInlineFilters } = this.props;
        if (settings) {
            const inlineFilters = settings.read('inlineFilters');
            state.inlineFiltersVisible = !disableInlineFilters && inlineFilters && inlineFilters.visible;
            this.updateInlineFilters(inlineFilters && inlineFilters.filters);
        }
        this.state = state;
        this.disposables = [];
        this.renderCellHeaderProps = {
            onColumnStyleUpdated: this.handleColumnStyleUpdated,
            showFilter: true,
            filters: false,
            onFilterChange: this.handleInlineFilterChange
        }
        this.handleScrollStarted = this.handleScrollStarted.bind(this);
        this.trackOperation = this.trackOperation.bind(this);
    }

    async componentWillMount(){
        Logger.info(`Waiting for registration of Dao ${this.props.daoName}`)
        this.dao = this.dao || await this.getDao(this.props.daoName);
        const dataSource = new DaoDataSource(this.dao);      
        this.disposables.push(dataSource.onDataRequested.subscribe(this.trackOperation));
        this.disposables.push(dataSource.columnsChanged.debounceTime(50).subscribe(this.setColumns.bind(this)));
        this.disposables.push(dataSource.onResized.debounceTime(500).subscribe(this.setSummary.bind(this)));
        this.disposables.push(dataSource.onChanged.debounceTime(500).subscribe(this.setSummary.bind(this)));
        this.setState({dataSource});
    }

    componentWillReceiveProps(newProps){
        if(!isEqual(newProps.options, this.props.options)){
            const {dataSource} = this.state;
            dataSource.updateSubscription(newProps.options);
        }
    }

    async updateOptionsAndWait(options){
        const {dataSource} = this.state;
        const start =  moment();
        await dataSource.undebouncedUpdateSubscrption(options);
        return moment.duration(moment() - start)
    }

    trackOperation(status){
        if(status){
            this.setState({timestamp : moment(), busy: status})
        }else{
            const {timestamp : startTime} = this.state;
            if(startTime){
                const now = moment();
                this.setState({elapsed: moment.duration(now - startTime), busy: status})
            }

        }
    }

    setColumns(columns){
        const {dataSource} = this.state;
        this.setState({columns : dataSource.columns});
    }
    setSummary(){
        const {dataSource} = this.state;
        const summary = {size: dataSource.size, options: dataSource.dao.options};
        this.setState({summary});
    }

    componentWillUnmount(){
        Logger.info("Unmounting grid");
        this.disposables.forEach(dis => dis.unsubscribe())
    }

    async getDao(daoName){
        const context = new GenericOperatorDaoContext(daoName, {});
        const dao = new Dao(context);
        return dao; 
    }

    renderTitleText({column}){
        return <div>{column.title}</div>
    }

    scrollRowIntoView(index){
        this.grid.scrollRowIntoView(index);
    }

    render() {
        let baselineHeight = ROW_HEIGHT + 2;
        const {dataSource, busy, summary ={}, elapsed} = this.state;
        if(!dataSource){
            return <div>Awaiting registration of data source</div>;
        }
        return <div style={{position : 'relative'}} className="flex flex-col">
                <div>{`${elapsed ? "Last Operation Took:" + elapsed.asSeconds() + " secs " : "" } Operator size is ${summary.size}. Options are ${JSON.stringify(summary.options)}`}</div>
                {busy ? <div style={{position : 'absolute', ...MODAL_STYLE}}><div style={{position : 'absolute', top : '50%', left: '50%', height: 400, width: 500}}><ScaleLoader size={50}/></div></div> : null}
                <div ref={grid => {this.gridContainer = grid}} className="flex flex-col">
                {this.gridContainer ? 
                <Grid ref={ grid => {this.grid = grid}}
                rowHeight={ROW_HEIGHT}
                headerHeight={baselineHeight}
                element={this.gridContainer}
                fontSize={baselineHeight}
                columns={this.state.columns}
                dataSource={dataSource}
                onColumnResized={this.handleColumnResized}
                onColumnHeaderClick={this.handleColumnHeaderClick.bind(this)}
                onContextMenu={this.handleContextMenu}
                onScrollStarted={this.handleScrollStarted}
                onColumnHeaderContextMenu={this.handleColumnHeaderContextMenu}
                onColumnStyleUpdated={this.handleColumnStyleUpdated}
                renderHeaderCell={renderColumnHeaderContent}
                renderHeaderCellProps={this.renderCellHeaderProps}
            ></Grid> : null }</div>
        </div>;
    }

    handleScrollStarted(){
        //this.setState({busy : true});
    }

    handleContextMenu(){
    }

    handleColumnHeaderContextMenu(){
    }

    handleInlineFilterChange({column, filter}){
        
    }

    handleColumnStyleUpdated(){
    }

    handleColumnResized(e) {
        const { settings } = this.props;
        if (settings) {
            const columns = settings.getColumns();
            columns[e.index].width = e.width;
            settings.setColumns(columns);
        }
    }

    handleColumnHeaderClick(column) {
        const {dataSource, busy} = this.state;
        if (column.sortable) {
            dataSource.sort(column.key);
        }
    }

}