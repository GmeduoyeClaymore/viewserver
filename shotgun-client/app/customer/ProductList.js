import React, {Component} from 'react';
import {View, StyleSheet, Text} from 'react-native';
import SearchBar from './SearchBar';
import ProductListItem from './ProductListItem';
import ListViewDataSink from '../common/ListViewDataSink';
import OperatorSubscriptionStrategy from '../common/OperatorSubscriptionStrategy';

export default class ProductList extends Component {
  /* static propTypes = {
    client: PropTypes.object.isRequired
  };*/

  constructor(props) {
    super(props);
    const {client} = this.props.screenProps;
    this.subscriptionStrategy = new OperatorSubscriptionStrategy(client, '/datasources/product/product');
  }

  search(productName) {
    if (this.listView) {
      this.listView.search('name like "*' + productName + '*"');
    }
  }

  render() {
    const rowView = (data) => {
      return (<ProductListItem product={data.item} navigation={this.props.navigation}/>);
    };

    return (
      <ListViewDataSink
        ref={ listView => { this.listView = listView;}}
        style={styles.container}
        subscriptionStrategy={this.subscriptionStrategy}
        rowView={rowView}
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

const Paging = () => <View><Text>Paging...</Text></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;
const LoadedAllItems = () => <View><Text>No More to display</Text></View>;

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    marginTop: 10
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
});
