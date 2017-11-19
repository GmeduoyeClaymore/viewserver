import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, Text} from 'react-native';
import SearchBar from './SearchBar';
import ProductListItem from './ProductListItem';
import {Spinner} from 'native-base';
import {updateSubscriptionAction, isAnyLoading, getLoadingErrors, getDaoOptions} from 'common/dao';
import PagingListView from '../../common/components/PagingListView';
import {connect} from 'react-redux';

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

  static navigationOptions = ({navigation}) => {
    const title = navigation.state.params !== undefined ? navigation.state.params.category : undefined;
    const navOptions = {title};
    //hide the header if this is not a sub category
    if (title == undefined){
      navOptions.header = null;
    }
    return navOptions;
  };

  constructor(props){
    super(props);
    const {navigation, screenProps} = this.props;
    const {dispatch} = screenProps;
    
    this.search = (searchText) => {
      dispatch(updateSubscriptionAction('productDao', {searchText}));
    };
    this.rowView = (p) => {
      return (<ProductListItem key={p.productId} product={p} navigation={navigation}/>);
    };
  }

  componentDidMount(){
    const {dispatch, options} = this.props;
    const {categoryId} = options;
    dispatch(updateSubscriptionAction('productDao', {categoryId}));
  }

  render(){
    const {rowView, search} = this;
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
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['productDao']),
  options: getDaoOptions(state, 'productDao'),
  errors: getLoadingErrors(state, ['productDao']), ...nextOwnProps
});

const ConnectedProductList =  connect(mapStateToProps)(ProductList);

export default ConnectedProductList;
