import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, TouchableHighlight, Image} from 'react-native';
import {Text, Spinner, Button, Container, Header, Title, Body, Left, Content} from 'native-base';
import { withRouter } from 'react-router';
import {LoadingScreen, PagingListView, ValidatingButton, Icon} from 'common/components';
import {isAnyLoading, getLoadingErrors, getDaoOptions, getNavigationProps, getDaoState} from 'common/dao';
import {connect} from 'custom-redux';
import {Redirect} from 'react-router-native';
import ProductListItem from './ProductListItem';
import yup from 'yup';

import {resolveProductCategoryIcon} from 'common/assets';

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    marginTop: 10
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  },
  subTitle: {
    marginTop: 25,
    marginBottom: 30
  },
  picture: {
    height: 50,
    width: 50
  }
});

class ProductCategoryList extends Component{
  static propTypes = {
    product: PropTypes.object,
    dispatch: PropTypes.func,
    screenProps: PropTypes.object,
    navigation: PropTypes.object
  };

  constructor(props){
    super(props);
    this.navigateToCategory = this.navigateToCategory.bind(this);
    this.rowView = this.rowView.bind(this);
  }

  rowView({item: row}){
    const {categoryId, category} = row;
 
    return <TouchableHighlight key={categoryId} style={{flex: 1, flexDirection: 'row'}} onPress={() => this.navigateToCategory(row)} underlayColor={'#EEEEEE'}>
      <View style={{flexDirection: 'row', flex: 1, padding: 0}}>
        <Image resizeMode="contain" source={resolveProductCategoryIcon(row.categoryId)}  style={styles.picture}/>
        <Text>{`${category}`}</Text>
      </View>
    </TouchableHighlight>;
  }


  navigateToCategory(category){
    const {history} = this.props;
    if (category.isLeaf) {
      history.push('/Customer/Checkout/ProductList', {category});
    } else {
      this.goToCategory(category);
    }
  }

  goToCategory(category){
    const {context} = this.props;
    context.setState({selectedCategory: category});
  }

  render(){
    const {busy, options, navigationStrategy, selectedProduct, selectedCategory = {}, history, rootProductCategory} = this.props;
    const {rowView} = this;

    if (selectedCategory.isLeaf){
      return <Redirect push={true} to={{pathname: '/Customer/Checkout/ProductList', state: {category: selectedCategory}}}/>;
    }

    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>No items to display</Text>;

    return busy ? <LoadingScreen text="Loading Product Categories" /> : <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='back-arrow' onPress={() => rootProductCategory.categoryId === selectedCategory.categoryId ?  navigationStrategy.prev() : this.goToCategory(rootProductCategory)} />
          </Button>
        </Left>
        <Body><Title>Select Product Category </Title></Body>
      </Header>
      <Content padded>
        <ProductListItem product={selectedProduct}/>
        <PagingListView
          style={styles.container}
          daoName='productCategoryDao'
          dataPath={['product', 'categories']}
          pageSize={10}
          options={{...options, parentCategoryId: selectedCategory && selectedCategory.categoryId ? selectedCategory.categoryId : rootProductCategory.categoryId}}
          history={history}
          rowView={rowView}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          headerView={() => null}
        />
        <ValidatingButton fullWidth paddedLeftRight iconRight onPress={() =>  navigationStrategy.next()} validateOnMount={true} validationSchema={yup.object(validationSchema)} model={selectedProduct}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow'/>
        </ValidatingButton>
      </Content>
    </Container>;
  }
}

const validationSchema = {
  productId: yup.string().required(),
};

const mapStateToProps = (state, nextOwnProps) => {
  const {context} = nextOwnProps;
  const {selectedContentType, selectedProduct, selectedCategory} = context.state;
  const {productCategory: rootProductCategory} = selectedContentType;
  const categories = getDaoState(state, ['product', 'categories'], 'productCategoryDao');

  return {
    categories,
    ...nextOwnProps,
    ...getNavigationProps(nextOwnProps),
    rootProductCategory,
    selectedProduct,
    selectedCategory: selectedCategory || rootProductCategory,
    busy: isAnyLoading(state, ['productDao', 'productCategoryDao']),
    options: getDaoOptions(state, 'productCategoryDao'),
    errors: getLoadingErrors(state, ['productDao', 'productCategoryDao']), ...nextOwnProps
  };
};

const ConnectedProductCategoryList =  withRouter(connect(mapStateToProps)(ProductCategoryList));

export default ConnectedProductCategoryList;

