import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, Text, TouchableHighlight} from 'react-native';
import {Spinner} from 'native-base';
import PagingListView from 'common/components/PagingListView';
import {isAnyLoading, getLoadingErrors, getDaoOptions} from 'common/dao';
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
const navFuncFactory =  ({categoryId, category, navigation, isLeaf, shouldNavigateToProductPage}) =>  () => {
  if (shouldNavigateToProductPage) {
    navigation.navigate('ProductList', {category});
  } else {
    navigation.navigate('ProductCategoryList', {parentCategoryId: categoryId, parentCategory: category, isLeaf});
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
    this.state = {};
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
    this.updateSubs(this.props);
  }

  componentWillReceiveProps(nextProps){
    const {parentCategoryId} = this.state;
    if (nextProps.navigation.state.params && nextProps.navigation.state.params.parentCategoryId != parentCategoryId){
      this.updateSubs(nextProps);
    }
  }

  updateSubs(props){
    const params = props.navigation.state.params || {};
    const {parentCategoryId = 'NONE'} = params;
    const options  = {...this.props.options, parentCategoryId};
    this.setState({options});
    //dispatch(updateSubscriptionAction('productCategoryDao', {parentCategoryId, parentCategory}, () => this.setState({parentCategoryId})));
  }

  render(){
    const {busy, errors} = this.props;
    const {options} = this.state;
    const {rowView} = this;
    return busy ? <Paging/> : <ErrorRegion errors={errors}><PagingListView
      style={styles.container}
      daoName='productCategoryDao'
      dataPath={['product', 'categories']}
      pageSize={10}
      options={options}
      rowView={rowView}
      paginationWaitingView={Paging}
      emptyView={NoItems}
      headerView={() => null}
    /></ErrorRegion>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['productDao', 'productCategoryDao']),
  options: getDaoOptions(state, 'productCategoryDao'),
  errors: getLoadingErrors(state, ['productDao', 'productCategoryDao']), ...nextOwnProps
});

const ConnectedProductCategoryList =  connect(mapStateToProps)(ProductCategoryList);

export default ConnectedProductCategoryList;

