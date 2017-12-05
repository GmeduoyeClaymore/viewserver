import React, { Component, PropTypes } from 'react';
import Grid from './grid';
import { isEqual, debounce } from 'lodash';
import DaoDataSource from './DaoDataSource';
import {DAO_REGISTRATION_CONTEXT} from 'custom-redux/DaoMiddleware'
import { renderColumnHeaderContent } from './ViewServerGridColumnHeader';

const CONTAINER_STYLE = { display: 'flex', flexDirection: 'column', flex: '1', overflow: 'hidden' };

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
        const dao = await this.getDao(this.props.daoName);
        const dataSink = await dao.getDataSink();
        const dataSource = new DaoDataSource(dao);
        this.setState({dataSource});
    }

    async getDao(daoName){
        const result = DAO_REGISTRATION_CONTEXT.daos[daoName];
        if(!result){
            result = await DAO_REGISTRATION_CONTEXT.registrationSubject.filter(d => d.name == daoName).take(1).toPromise();
        }
        return result;
    }

    render() {
        let baselineHeight = this.context.userHeight + 2;
        const {dataSource} = this.state;
        if(!dataSource){
            return <div>Awaiting registration of data source</div>;
        }
        return <Grid ref={ grid => {this.grid = grid}}
            rowHeight={this.context.userHeight}
            headerHeight={baselineHeight}
            fontSize={baselineHeight}
            columns={this.state.columns}
            dataSource={this.dataSource}
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
        ></Grid>
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