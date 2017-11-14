import React, {Component} from 'react';
import {View, ScrollView, StyleSheet, Dimensions } from 'react-native';
import PropTypes from 'prop-types';

const styles = StyleSheet.create({
  contentContainer: {
    paddingVertical: 20
  }
});

export default class PagingListView extends Component{
    static propTypes = {
      dao: PropTypes.object.isRequired,
      data: PropTypes.array.isRequired,
      busy: PropTypes.bool.isRequired,
      rowView: PropTypes.func.isRequired,
      paginationWaitingView: PropTypes.func.isRequired,
      emptyView: PropTypes.func.isRequired
    };

    constructor(props){
      super(props);
      this._onScroll = this._onScroll.bind(this);
      this.dao = props.dao;
      this.state = {pageSize: props.pageSize, limit: props.pageSize};
    }

    componentWillMount(){
      this.dao.subscribe();
    }

    _onScroll(e){
      const windowHeight = Dimensions.get('window').height;
      const height = e.nativeEvent.contentSize.height;
      const offset = e.nativeEvent.contentOffset.y;
      if ( windowHeight + offset >= (height * 0.75) ){
        if (!this.props.busy){
          this.loadNextPage();
        }
      }
    }
    
    loadNextPage(){
      const newLimit = this.state.limit + this.state.pageSize;
      const pagingSuccess =  this.dao.page(0, newLimit);

      if (pagingSuccess) {
        this.setState({limit: newLimit});
      }
    }
    
    renderItem = (item) => this.props.rowView(item);
    
    render() {
      const { data, emptyView, paginationWaitingView, headerView: HeaderView } = this.props;
      return (
        <View style={{flex: 1, flexDirection: 'column'}}>
          <HeaderView/>
            {(data.length === 0 && !this.props.busy)  ? emptyView() : <ScrollView contentContainerStyle={styles.contentContainer} style={{flex: 1, flexDirection: 'column'}} onScroll={this._onScroll}>
            {data.map( c => this.renderItem(c))}
            {this.props.busy ? paginationWaitingView() : null}
          </ScrollView >}
        </View>
      );
    }
}
