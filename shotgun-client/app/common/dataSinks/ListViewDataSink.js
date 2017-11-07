import React, {Component, PropTypes} from 'react';
import {View, ScrollView, StyleSheet, Dimensions } from 'react-native';
import Logger from '../../viewserver-client/Logger';
import DataSink from './DataSink';

const styles = StyleSheet.create({
  contentContainer: {
    paddingVertical: 20
  }
});

export default class ListViewDataSink extends DataSink(Component){
    static propTypes = {
      subscriptionStrategy: PropTypes.object.isRequired,
      rowView: PropTypes.func.isRequired,
      paginationWaitingView: PropTypes.func.isRequired,
      emptyView: PropTypes.func.isRequired,
      paginationAllLoadedView: PropTypes.func.isRequired,
      refreshable: PropTypes.bool.isRequired,
      enableEmptySections: PropTypes.bool.isRequired,
      removeRow: PropTypes.func,
      options: PropTypes.object
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

    static INITIAL_STATE = {
      list: [],
      page: 1,
      hasNextPage: true,
      refreshing: false,
      limit: 10
    };

    constructor(props){
      super(props);
      this._onScroll = this._onScroll.bind(this);
      this.options = Object.assign({}, ListViewDataSink.DEFAULT_OPTIONS, props.options);
      this.state = {options: this.options, ...ListViewDataSink.INITIAL_STATE};
    }

    componentWillMount(){
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
      const height = e.nativeEvent.contentSize.height;
      const offset = e.nativeEvent.contentOffset.y;
      if ( windowHeight + offset >= (height * 0.75) ){
        if (isNextPageLoading){
          this.setState({loadingQueued: true});
        } else {
          this.loadNextPage();
        }
      }
    }
    
    loadNextPage(){
      //TODO - not sure this is quite working - keeps printing this after the first scroll
      if (this.rows.length >= this.totalRowCount){
        Logger.info('Reached end of viewport');
        return;
      }
      //TODO - tidy this up a bit
      const { limit } = this.state;
      const newLimit = limit + this.state.options.limit;
      const newOptions = {...this.state.options};
      newOptions.limit = newLimit;
      this.props.subscriptionStrategy.update(this, newOptions);
      this.setState({options: newOptions, isNextPageLoading: true});
    }
    
    updateOptions(options){
      this.options = Object.assign({}, ListViewDataSink.DEFAULT_OPTIONS, options);
      this.props.subscriptionStrategy.update(this, this.options);
      this.setState({options: this.options});
    }

    refresh(){
      this.props.subscriptionStrategy.update(this.props.client, this.options);
      this.setState({options: this.options});
    }

    renderItem = (item) => this.props.rowView(item);
    
    render() {
      const { rows } = this;
      const { emptyView, headerView: HeaderView } = this.props;
      return (
        <View style={{flex: 1, flexDirection: 'column'}}>
          <HeaderView/>
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
