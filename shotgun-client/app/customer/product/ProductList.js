import React from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, Text} from 'react-native';
import SearchBar from './SearchBar';
import ProductListItem from './ProductListItem';
import {Spinner} from 'native-base';
import PagingListView from '../../common/components/PagingListView';
import {updateSubscriptionAction} from 'common/dao';

const ProductList = ({navigation}) => {
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
  const categoryFilterExpression = categoryId !== undefined ? `categoryId ==\"${categoryId}\"` : 'true == true';

  const search = (searchText) => {
      const productFilter = searchText != '' ? `name like "*${searchText}*"` : '';
      const filterExpression = options.filterExpression == '' ? productFilter : `${productFilter} && ${categoryFilterExpression}`;
      dispatch(updateSubscriptionAction('productDao', {filterExpression}));
  };

  const Paging = () => <View><Spinner /></View>;
  const NoItems = () => <View><Text>No items to display</Text></View>;
  const rowView = (p) => {
    return (<ProductListItem key={p.productId} product={p} navigation={navigation}/>);
  };

  return <PagingListView
    style={styles.container}
    daoName='productDao'
    dataPath={['product', 'products']}
    pageSize={10}
    rowView={rowView}
    paginationWaitingView={Paging}
    emptyView={NoItems}
    headerView={() => <SearchBar onChange={search.bind(this)} />}
  />;
};

ProductList.propTypes = {
  product: PropTypes.object,
  dispatch: PropTypes.func,
  screenProps: PropTypes.object,
  navigation: PropTypes.object,
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
