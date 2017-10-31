import React, {Component, PropTypes} from 'react';
import {View, ScrollView, StyleSheet, Dimensions } from 'react-native';
import Logger from '../../viewserver-client/Logger';
import DataSink from './DataSink';

const styles = StyleSheet.create({
  contentContainer: {
    paddingVertical: 20
  }
});

export default class DataSinkListView extends DataSink(Component){
    static propTypes = {
      subscriptionStrategy: PropTypes.object.isRequired,
      rowView: PropTypes.func.isRequired,
      paginationWaitingView: PropTypes.func.isRequired,
      emptyView: PropTypes.func.isRequired,
      paginationAllLoadedView: PropTypes.func.isRequired,
      refreshable: PropTypes.bool.isRequired,
      enableEmptySections: PropTypes.bool.isRequired,
      removeRow: PropTypes.func,
    };

    static DEFAULT_OPTIONS = {
      offset: 0,
      limit: 20,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      filterExpression: undefined,
      flags: undefined
    };

    constructor(props){
      super(props);
      this._onScroll = this._onScroll.bind(this);
    }

    state = {
      list: [],
      page: 1,
      hasNextPage: true,
      refreshing: false,
      options: DataSinkListView.DEFAULT_OPTIONS
    };
    
    componentDidMount() {
      this.subscribe();
    }

    subscribe(){
      this.setState({
        isNextPageLoading: true
      });
      this.props.subscriptionStrategy.subscribe(this, this.state.options);
    }

    _onScroll(e){
      const windowHeight = Dimensions.get('window').height;
      const {isNextPageLoading} = this.state;
      height = e.nativeEvent.contentSize.height;
      offset = e.nativeEvent.contentOffset.y;
      if ( windowHeight + offset >= (height * 0.75) ){
        if (isNextPageLoading){
          this.setState({loadingQueued: true});
        } else {
          this.loadNextPage();
        }
      }
    }
    
    loadNextPage(){
      if (this.rows.length >= this.totalRowCount){
        Logger.info('Reached end of viewport');
        return;
      }
      const { limit } = this.state.options;
      const newLimit = limit + DataSinkListView.DEFAULT_OPTIONS.limit;
      const newOptions = {...this.state.options};
      newOptions.limit = newLimit;
      this.props.subscriptionStrategy.update(this, newOptions);
      this.setState({options: newOptions, isNextPageLoading: true});
    }
    
    search(expression){
      const newOptions = {...this.state.options};
      newOptions.filterExpression = expression;
      this.props.subscriptionStrategy.update(this, newOptions);
      this.setState({options: newOptions});
    }
    
    refresh(){
      this.props.subscriptionStrategy.update(this.props.client, DataSinkListView.DEFAULT_OPTIONS);
      this.setState({options: DataSinkListView.DEFAULT_OPTIONS});
    }

    renderItem = (item) => {
      const RowView = this.props.rowView;
      return <RowView key={item.productId} style={{flex: 1}} item={item}/>;
    };
    
    render() {
      const { rows } = this;
      const { emptyView, headerView: HeaderView } = this.props;
      return (
        <View style={{flex: 1, flexDirection: 'column'}}>
          <View style={{height: 60}}>
            <HeaderView/>
          </View>
          {(rows.length === 0)  ? emptyView() : <ScrollView contentContainerStyle={styles.contentContainer} style={{flex: 1, flexDirection: 'column'}} onScroll={this._onScroll}>
            {this.rows.map( c => this.renderItem(c))}
          </ScrollView >}
        </View>
      );
    }

    onSnapshotComplete(){
      this.setState({
        isNextPageLoading: false
      });
    }

    onRowAdded(rowId, row){
      super.onRowAdded(rowId, row);
      const {isNextPageLoading, loadingQueued} = this.state;
      const { limit } = this.state.options;
      if (this.rows.length >= limit && isNextPageLoading){
        if (loadingQueued){
          this.loadNextPage();
        }
        this.setState({
          isNextPageLoading: false,
          loadingQueued: false
        });
      }
    }
}
