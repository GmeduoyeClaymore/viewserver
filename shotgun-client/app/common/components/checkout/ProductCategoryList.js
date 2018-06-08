import React, {Component} from 'react';
import {View, TouchableHighlight} from 'react-native';
import {Text, Spinner, Button, Container, Header, Title, Body, Left, Content} from 'native-base';
import {LoadingScreen, PagingListView, ValidatingButton, Icon} from 'common/components';
import {isAnyLoading, getLoadingErrors, getDaoOptions, getNavigationProps, getDaoState} from 'common/dao';
import {withExternalState, Redirect} from 'custom-redux';
import ProductListItem from './ProductListItem';
import yup from 'yup';

class ProductCategoryList extends Component{
  rowView = ({item: row}) => {
    const {categoryId, category} = row;
 
    return <TouchableHighlight key={categoryId} style={{flex: 1, flexDirection: 'row'}} onPress={() => this.navigateToCategory(row)} underlayColor={'#EEEEEE'}>
      <View style={{flexDirection: 'row', flex: 1, padding: 0}}>
        <Icon name={row.categoryId}/>
        <Text>{`${category}`}</Text>
      </View>
    </TouchableHighlight>;
  }


  navigateToCategory(category){
    const {history, parentPath} = this.props;
    if (category.isLeaf) {
      history.push(`${parentPath}/ProductList`, {category});
    } else {
      this.goToCategory(category);
    }
  }

  goToCategory = (selectedCategory) => {
    const {selectedCategory: parentSelectedCategory} = this.props;
    this.setState({selectedCategory, parentSelectedCategory});
  }

  render(){
    const {busy, next, selectedProduct, selectedCategory = {}, history, rootProductCategory, defaultOptions, parentPath} = this.props;
    const {rowView} = this;

    if (selectedCategory.isLeaf){
      return <Redirect push={true} to={{pathname: `${parentPath}/ProductList`, state: {category: selectedCategory}}}/>;
    }

    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>No items to display</Text>;

    return busy ? <LoadingScreen text="Loading Product Categories" /> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => rootProductCategory.categoryId === selectedCategory.categoryId ?  history.goBack() : this.goToCategory(rootProductCategory)}>
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
      <ValidatingButton fullWidth paddedBottomLeftRight iconRight arrow={true} onPress={() =>  history.push(next)} validateOnMount={true} validationSchema={yup.object(validationSchema)} model={selectedProduct}>
        <Text uppercase={false}>Continue</Text>
      </ValidatingButton>
    </Container>;
  }
}

const validationSchema = {
  productId: yup.string().required(),
};

const styles = {
  pagingListView: {
    backgroundColor: '#FFFFFF',
    marginTop: 10
  }
};

const mapStateToProps = (state, initialProps) => {
  const {selectedContentType, selectedProduct, selectedCategory} = initialProps;
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

const ConnectedProductCategoryList =  withExternalState(mapStateToProps)(ProductCategoryList);
export default ConnectedProductCategoryList;

