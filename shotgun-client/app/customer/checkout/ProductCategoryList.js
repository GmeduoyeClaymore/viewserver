import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, Text, TouchableHighlight} from 'react-native';
import {Spinner} from 'native-base';
import PagingListView from 'common/components/PagingListView';
import {updateSubscriptionAction, isAnyLoading, getLoadingErrors, getDaoOptions} from 'common/dao';
import {connect} from 'react-redux';
import ErrorRegion from 'common/components/ErrorRegion';
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

const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;
const navFuncFactory =  ({categoryId, category, history, isLeaf, shouldNavigateToProductPage}) =>  () => {
  if (shouldNavigateToProductPage) {
    history.push('/Customer/Checkout/ProductList', {category});
  } else {
    history.push('/Customer/Checkout/ProductCategoryList', {parentCategoryId: categoryId, parentCategory: category, isLeaf});
  }
};

class ProductCategoryList extends Component{
  static propTypes = {
    product: PropTypes.object,
    dispatch: PropTypes.func,
    screenProps: PropTypes.object,
    navigation: PropTypes.object
  };

  static navigationOptions = ({navigation}) => {
    const title = navigation.state.params !== undefined ? navigation.state.params.parentCategory : undefined;
    const navOptions = {title};
    if (title == undefined){
      navOptions.header = null;
    }
    return navOptions;
  };

  constructor(props){
    super(props);
    const {screenProps, navigation} = this.props;
    const {dispatch} = screenProps;
    const params = navigation.state.params || {};
    const {isLeaf: shouldNavigateToProductPage} = params;
    this.rowView = (row) => {
      const {categoryId, category, isLeaf} = row;
      return <TouchableHighlight key={categoryId} style={{flex: 1, flexDirection: 'row'}} onPress={navFuncFactory({dispatch, navigation, categoryId, category, isLeaf, shouldNavigateToProductPage})} underlayColor={'#EEEEEE'}>
      <View style={{flexDirection: 'column', flex: 1, padding: 0}}>
        <Text>{`${category}`}</Text>
      </View>
      </TouchableHighlight>;
    };
  }

  componentDidMount(){
    const {navigation, dispatch} = this.props;
    const params = navigation.state.params || {};
    const {parentCategoryId = 'NONE', parentCategory = undefined} = params;
    dispatch(updateSubscriptionAction('productCategoryDao', {parentCategoryId, parentCategory}));
  }

  render(){
    const {busy, errors, options} = this.props;
    const {rowView} = this;
    return busy ? <Paging/> : <View style={{flexDirection: 'column', flex: 1, padding: 0}}><ErrorRegion errors={errors}/><PagingListView
      style={styles.container}
      daoName='productCategoryDao'
      dataPath={['product', 'categories']}
      pageSize={10}
      options={options}
      rowView={rowView}
      paginationWaitingView={Paging}
      emptyView={NoItems}
      headerView={() => null}
    /></View>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['productDao', 'productCategoryDao']),
  options: getDaoOptions(state, 'productCategoryDao'),
  errors: getLoadingErrors(state, ['productDao', 'productCategoryDao']), ...nextOwnProps
});

const ConnectedProductCategoryList =  connect(mapStateToProps)(ProductCategoryList);

export default ConnectedProductCategoryList;

