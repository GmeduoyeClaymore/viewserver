import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, Text} from 'react-native';
import SearchBar from './SearchBar';
import ProductListItem from './ProductListItem';
import {Spinner} from 'native-base';
import {updateSubscriptionAction, isAnyLoading, getLoadingErrors, getNavigationProps} from 'common/dao';
import {PagingListView} from 'common/components';
import {connect} from 'custom-redux';

const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;

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

class ProductList extends Component{
  static propTypes = {
    product: PropTypes.object,
    screenProps: PropTypes.object,
    navigation: PropTypes.object,
  };

  static navigationOptions = ({category}) => {
    const navOptions = {title: category};
    //hide the header if this is not a sub category
    if (title == undefined){
      navOptions.header = null;
    }
    return navOptions;
  };

  constructor(props){
    super(props);
    const {history, dispatch, context} = this.props;
    this.search = (searchText) => {
      dispatch(updateSubscriptionAction('productDao', {searchText}));
    };
    this.search = this.search.bind(this);
    this.rowView = ({item: p}) => {
      return (<ProductListItem key={p.productId} product={p} history={history} context={context}/>);
    };
  }

  render(){
    const {rowView, search, props} = this;
    const {context, options} = props;
    return <PagingListView
      style={styles.container}
      daoName='productDao'
      dataPath={['product', 'products']}
      pageSize={10}
      options={options}
      context={context}
      rowView={rowView}
      paginationWaitingView={Paging}
      emptyView={NoItems}
      headerView={() => <SearchBar onChange={search} />}
    />;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const navProps = getNavigationProps(nextOwnProps);
  const {category = {}} = navProps;
  const {categoryId} = category;
  const options  = {categoryId};
  return {
    ...navProps,
    busy: isAnyLoading(state, ['productDao']),
    options,
    errors: getLoadingErrors(state, ['productDao']), ...nextOwnProps
  };
};

const ConnectedProductList =  connect(mapStateToProps)(ProductList);

export default ConnectedProductList;
