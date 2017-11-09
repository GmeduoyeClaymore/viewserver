import React, {PropTypes} from 'react';
import {View, StyleSheet, Text} from 'react-native';
import SearchBar from './SearchBar';
import ProductListItem from './ProductListItem';
import ListViewDataSink from '../../common/dataSinks/ListViewDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';

const ProductList = ({screenProps, navigation}) => {
  const {client} = screenProps;
  const subscriptionStrategy = new DataSourceSubscriptionStrategy(client, 'product');
  let myListView;

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

  const categoryId = navigation.state.params && navigation.state.params.categoryId;
  const filterExpression = categoryId !== undefined ? `categoryId ==\"${categoryId}\"` : 'true == true';
  const options = {
    filterExpression
  };

  const search = (searchText) => {
    if (myListView) {
      const productFilter = searchText != '' ? `name like "*${searchText}*"` : '';
      options.filterExpression = options.filterExpression == '' ? productFilter : `${filterExpression} && ${productFilter}`;
      myListView.updateOptions(options);
    }
  };

  const Paging = () => <View><Text>Paging...</Text></View>;
  const NoItems = () => <View><Text>No items to display</Text></View>;
  const LoadedAllItems = () => <View><Text>No More to display</Text></View>;
  const rowView = (product) => {
    return (<ProductListItem key={product.productId} product={product} navigation={navigation}/>);
  };

  return <ListViewDataSink
    ref={ listView => { myListView = listView;}}
    style={styles.container}
    subscriptionStrategy={subscriptionStrategy}
    options={options}
    rowView={rowView}
    paginationWaitingView={Paging}
    emptyView={NoItems}
    paginationAllLoadedView={LoadedAllItems}
    refreshable={true}
    enableEmptySections={true}
    renderSeparator={(sectionId, rowId) => <View key={rowId} style={styles.separator} />}
    headerView={() => <SearchBar onChange={search.bind(this)} />}
  />;
};

ProductList.propTypes = {
  screenProps: PropTypes.object,
  navigation: PropTypes.object
};

ProductList.navigationOptions = ({navigation}) => {
  const title = navigation.state.params !== undefined ? navigation.state.params.category : undefined;
  const navOptions = {title};

  //hide the header if this is not a sub category
  if (title == undefined){
    navOptions.header = null;
  }
  return navOptions;
};

export default ProductList;


