import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, TouchableHighlight, Image} from 'react-native';
import {Text, Spinner, Button, Container, Header, Title, Body, Left, Content} from 'native-base';
import {LoadingScreen, PagingListView, ValidatingButton, Icon} from 'common/components';
import {isAnyLoading, getLoadingErrors, getDaoOptions, getNavigationProps, getDaoState} from 'common/dao';
import {connect} from 'custom-redux';
import {Redirect} from 'react-router-native';
import ProductListItem from './ProductListItem';
import yup from 'yup';
import {resolveProductCategoryIcon} from 'common/assets';

class ProductCategoryList extends Component{
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
    const {history, match} = this.props;
    if (category.isLeaf) {
      history.push(`${match.path}/ProductList`, {category});
    } else {
      this.goToCategory(category);
    }
  }

  goToCategory(selectedCategory){
    const {context} = this.props;
    const {selectedCategory: parentSelectedCategory} = context.state;
    context.setState({selectedCategory, parentSelectedCategory});
  }

  render(){
    const {busy, navigationStrategy, selectedProduct, selectedCategory = {}, history, rootProductCategory, defaultOptions} = this.props;
    const {rowView} = this;

    if (selectedCategory.isLeaf){
      return <Redirect push={true} to={{pathname: `${match.path}/ProductList`, state: {category: selectedCategory}}}/>;
    }

    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>No items to display</Text>;

    return busy ? <LoadingScreen text="Loading Product Categories" /> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => rootProductCategory.categoryId === selectedCategory.categoryId ?  navigationStrategy.prev() : this.goToCategory(rootProductCategory)}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Category</Title></Body>
      </Header>
      <Content padded>
        <ProductListItem product={selectedProduct}/>
        <PagingListView
          style={styles.pagingListView}
          daoName='productCategoryDao'
          dataPath={['product', 'categories']}
          pageSize={10}
          options={defaultOptions}
          history={history}
          rowView={rowView}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          headerView={() => null}
        />
      </Content>
      <ValidatingButton fullWidth paddedBottom iconRight onPress={() =>  navigationStrategy.next()} validateOnMount={true} validationSchema={yup.object(validationSchema)} model={selectedProduct}>
        <Text uppercase={false}>Continue</Text>
        <Icon next name='forward-arrow'/>
      </ValidatingButton>
    </Container>;
  }
}

const validationSchema = {
  productId: yup.string().required(),
};

const mapStateToProps = (state, initialProps) => {
  const {context} = initialProps;
  const {selectedContentType, selectedProduct, selectedCategory} = context.state;
  const {productCategory: rootProductCategory} = selectedContentType;
  const categories = getDaoState(state, ['product', 'categories'], 'productCategoryDao');

  const defaultOptions = {
    ...getDaoOptions(state, 'productCategoryDao'),
    parentCategoryId: selectedCategory && selectedCategory.categoryId ? selectedCategory.categoryId : rootProductCategory.categoryId
  };

  return {
    categories,
    ...initialProps,
    ...getNavigationProps(initialProps),
    rootProductCategory,
    selectedContentType,
    selectedProduct,
    selectedCategory: selectedCategory || rootProductCategory,
    busy: isAnyLoading(state, ['productDao', 'productCategoryDao']),
    defaultOptions,
    errors: getLoadingErrors(state, ['productDao', 'productCategoryDao']), ...initialProps
  };
};


ProductCategoryList.propTypes = {
  product: PropTypes.object,
  dispatch: PropTypes.func,
  screenProps: PropTypes.object,
  navigation: PropTypes.object
};

const styles = {
  pagingListView: {
    backgroundColor: '#FFFFFF',
    marginTop: 10
  },
  picture: {
    height: 50,
    width: 50
  }
};

const ConnectedProductCategoryList =  connect(mapStateToProps)(ProductCategoryList);

export default ConnectedProductCategoryList;
