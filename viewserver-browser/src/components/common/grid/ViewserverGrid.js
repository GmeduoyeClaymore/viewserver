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


const CONTAINER_STYLE = { display: 'flex', flexDirection: 'column', flex: '1', overflow: 'hidden' };
const MODAL_STYLE = {
    zIndex: 1, /* Sit on top */
    left: '50%',
    top: '50%',
    width: '100%', /* Full width */
    height: '100%', /* Full height */
    overflow: 'auto', /* Enable scroll if needed */
    backgroundColor: 'rgb(0,0,0)', /* Fallback color */
    backgroundColor: 'rgba(255,255,255,0.4)' /* Black w/ opacity */
}
const ROW_HEIGHT = 12;
function setState(state) {
    this.state = {
        ...this.state,
        ...state
    };
}

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
        this.setState = setState;
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
     
    }

    async componentWillMount(){
        Logger.info(`Waiting for registration of Dao ${this.props.daoName}`)
        this.dao = this.dao || await this.getDao(this.props.daoName);
        const dataSource = new DaoDataSource(this.dao);      
        dataSource.columnsChanged.subscribe(this.setColumns.bind(this));
        this.setState({dataSource});
    }

    componentWillReceiveProps(newProps){
        if(!isEqual(newProps.options, this.props.options)){
            this.busy(this.dao.updateSubscription(newProps.options));
        }
    }

    async busy(promise){
        try{
            this.setState({busy : true})
            await promise;
        }finally{
            this.setState({busy : false})
        }
        
    }
    setColumns(columns){
        const {dataSource} = this.state;
        this.setState({columns : dataSource.columns});
    }

    componentWillUnmount(){
        Logger.info("Unmounting grid");
    }

    async getDao(daoName){
        const context = new GenericOperatorDaoContext(daoName, {});
        const dao = new Dao(context);
        const {updateSubscription} = dao;
        dao.updateSubscription = opt => this.busy(updateSubscription(opt));
        return dao; 
    }

    render() {
        let baselineHeight = ROW_HEIGHT + 2;
        const {dataSource, busy} = this.state;
        if(!dataSource){
            return <div>Awaiting registration of data source</div>;
        }
        return <div style={{position : 'relative'}} ref={grid => {this.gridContainer = grid}} className="flex flex-col">
            {busy ? <div style={{position : 'absolute', ...MODAL_STYLE}}><ScaleLoader/></div> : null}
            {this.gridContainer ? 
            <Grid ref={ grid => {this.grid = grid}}
            rowHeight={ROW_HEIGHT}
            headerHeight={baselineHeight}
            element={this.gridContainer}
            fontSize={baselineHeight}
            columns={this.state.columns}
            dataSource={dataSource}
            onColumnResized={this.handleColumnResized}
            onColumnHeaderClick={this.handleColumnHeaderClick}
            onContextMenu={this.handleContextMenu}
            onColumnHeaderContextMenu={this.handleColumnHeaderContextMenu}
            onColumnStyleUpdated={this.handleColumnStyleUpdated}
            renderHeaderCell={renderColumnHeaderContent}
            renderHeaderCellProps={{
                    onColumnStyleUpdated: this.handleColumnStyleUpdated,
                    showFilter: this.state.inlineFiltersVisible,
                    filters: this.state.inlineFilters,
                    onFilterChange: this.handleInlineFilterChange
                }}
        ></Grid> : null }</div>
    }

    handleContextMenu(){
    }

    handleColumnHeaderContextMenu(){
    }

    handleInlineFilterChange(){
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
        const { settings } = this.props;
        if (settings && column.sortable && settings.toggleSort(column.key, true)) {
            column = settings.getColumn(column.key);
            this.dataSource.sort(column.key, column.sorted);
            // do we really need to update the columns?
            this._updateColumns(this.props);
        }
    }

}