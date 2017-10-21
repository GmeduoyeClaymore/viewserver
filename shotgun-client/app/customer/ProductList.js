import React, {Component,PropTypes} from 'react';
import {View, ListView, StyleSheet, Text} from 'react-native';
import SearchBar from './SearchBar';
import ProductListItem from './ProductListItem';
import ListViewDataSink from '../common/ListViewDataSink';
import OperatorSubscriptionStrategy from '../common/OperatorSubscriptionStrategy';


const Paging = () => <View><Text>Paging...</Text></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;
const LoadedAllItems = () => <View><Text>No More to display</Text></View>;

export default class ProductList extends Component {

  static propTypes = {
    client : PropTypes.object.isRequired
  }

  constructor(props) {
    super(props);
    this.subscriptionStrategy = new OperatorSubscriptionStrategy(this.props.client,'/datasources/product/product')
  }

  search(productName) {
    if (this.listView)
    {
        this.listView.search("P_name like \"*" + productName + "*\"");
    }
  }

  render() {
    return (
      <ListViewDataSink 
        ref={ listView => { this.listView = listView}}
        style={styles.container}
        subscriptionStrategy={this.subscriptionStrategy}
        rowView={(data) => (<ProductListItem navigator={this.props.navigator} {...data} />)}
        paginationWaitingView={Paging}
        emptyView={NoItems}
        paginationAllLoadedView={LoadedAllItems}
        refreshable={true}
        enableEmptySections={true}
        renderSeparator={(sectionId, rowId) => <View key={rowId} style={styles.separator} />}
        headerView={() => <SearchBar onChange={this.search.bind(this)} />}
      />
    );
  }
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    marginTop: 60
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
});
