import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, Text, TouchableHighlight} from 'react-native';
import {Spinner} from 'native-base';
import PagingListView from 'common/components/PagingListView';
import {isAnyLoading, getLoadingErrors, getDaoOptions, updateSubscriptionAction} from 'common/dao';
import {connect} from 'react-redux';
import ErrorRegion from 'common/components/ErrorRegion';
import backIcon from '../../common/assets/back.png';
import ActionButton from '../../common/components/ActionButton';

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    marginTop: 10
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    height: 30
  }
});

const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;

class ProductCategoryList extends Component{
  static propTypes = {
    product: PropTypes.object,
    dispatch: PropTypes.func,
    screenProps: PropTypes.object,
    navigation: PropTypes.object
  };

  navFuncFactory({parentCategoryId, categoryId, category, navigation, isLeaf}) {
    if (isLeaf == 'true') { //TODO - get isLeaf to be a proper bool in the ViewServer
      navigation.navigate('ProductList', {category});
    } else {
      this.setNavigationState({parentCategoryId: categoryId, parentCategory: category, grandparentCategoryId: parentCategoryId});
    }
  }

  setNavigationState({parentCategoryId, parentCategory, grandparentCategoryId}){
    this.setState({parentCategoryId, parentCategory, grandparentCategoryId});
  }

  constructor(props){
    super(props);
    const {navigation} = this.props;
    this.state = {parentCategoryId: 'NONE', parentCategory: undefined};
    this.rowView = (row) => {
      const {categoryId, category, isLeaf, parentCategoryId} = row;
      return <TouchableHighlight key={categoryId} style={{flex: 1, flexDirection: 'row'}} onPress={() => this.navFuncFactory({navigation, categoryId, category, parentCategoryId, isLeaf})} underlayColor={'#EEEEEE'}>
      <View style={{flexDirection: 'column', flex: 1, padding: 0}}>
        <Text>{`${category}`}</Text>
      </View>
      </TouchableHighlight>;
    };
  }

  componentDidMount(){
    this.updateSubs(this.props, 'NONE');
  }

  shouldComponentUpdate(nextProps, nextState){
    if (this.state.parentCategoryId !== nextState.parentCategoryId) {
      this.updateSubs(nextProps, nextState.parentCategoryId);
    }
    return true;
  }

  updateSubs(props, parentCategoryId){
    const {screenProps: {dispatch}} = props;
    dispatch(updateSubscriptionAction('productCategoryDao', {parentCategoryId}));
  }

  render(){
    const {busy, errors, options} = this.props;
    const {parentCategory, grandparentCategoryId} = this.state;
    const {rowView} = this;
    return busy ? <Paging/> :
      <View  style={{flexDirection: 'column', flex: 1}}>
        {parentCategory ? <View style={styles.header}><ActionButton buttonText={null} icon={backIcon} action={() => this.setNavigationState({parentCategoryId: grandparentCategoryId})}/><Text>{parentCategory}</Text></View> : null}
      <ErrorRegion errors={errors}>
      <PagingListView
      style={styles.container}
      daoName='productCategoryDao'
      dataPath={['product', 'categories']}
      pageSize={11}
      options={options}
      rowView={rowView}
      paginationWaitingView={Paging}
      emptyView={NoItems}
      headerView={() => null}/>
      </ErrorRegion>
      </View>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['productDao', 'productCategoryDao']),
  options: getDaoOptions(state, 'productCategoryDao'),
  errors: getLoadingErrors(state, ['productDao', 'productCategoryDao']), ...nextOwnProps
});

const ConnectedProductCategoryList =  connect(mapStateToProps)(ProductCategoryList);

export default ConnectedProductCategoryList;

