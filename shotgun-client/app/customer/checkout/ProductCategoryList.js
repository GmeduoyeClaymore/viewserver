import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, TouchableHighlight} from 'react-native';
import {Text, Spinner, Button, Container, Header, Title, Body, Left, Content} from 'native-base';
import { withRouter } from 'react-router';
import {LoadingScreen, PagingListView, ErrorRegion, ValidatingButton, Icon} from 'common/components';
import {isAnyLoading, getLoadingErrors, getDaoOptions, getNavigationProps, getDaoSize} from 'common/dao';
import {connect} from 'custom-redux';
import ProductListItem from './ProductListItem';
import yup from 'yup';

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
    this.redirectIfOnlyOneCategory(row);
    return <TouchableHighlight key={categoryId} style={{flex: 1, flexDirection: 'row'}} onPress={() => this.navigateToCategory({category: row})} underlayColor={'#EEEEEE'}>
      <View style={{flexDirection: 'column', flex: 1, padding: 0}}>
        <Text>{`${category}`}</Text>
      </View>
    </TouchableHighlight>;
  }

  redirectIfOnlyOneCategory(category){
    const {numberCategories, busy} = this.props;
    if (!busy && numberCategories && numberCategories == 1 && category.isLeaf){
      history.push('/Customer/Checkout/ProductList', {category});
    }
  }

  navigateToCategory({category}){
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
    const {busy, errors, options, navigationStrategy, selectedProduct, selectedCategory = {}, history, rootProductCategory} = this.props;
    const {rowView} = this;

    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>No items to display</Text>;

    return busy ? <LoadingScreen text="Loading Product Categories" /> : <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='arrow-back' onPress={() => rootProductCategory.categoryId === selectedCategory.categoryId ?  navigationStrategy.prev() : this.goToCategory(rootProductCategory)} />
          </Button>
        </Left>
        <Body><Title>Select Product Category</Title></Body>
      </Header>
      <Content padded>
        <ProductListItem product={selectedProduct}/>
        <ErrorRegion errors={errors}>
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
        </ErrorRegion>
        <ValidatingButton fullWidth paddedLeftRight iconRight onPress={() =>  navigationStrategy.next()} validateOnMount={true} validationSchema={yup.object(validationSchema)} model={selectedProduct}>
          <Text uppercase={false}>Continue</Text>
          <Icon name='arrow-forward'/>
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
  const {rootProductCategory: rootProductCategoryId} = selectedContentType;
  const rootProductCategory = {};
  const numberCategories = getDaoSize(state, 'productCategoryDao');
  rootProductCategory.categoryId = rootProductCategoryId;
  return {
    numberCategories,
    ...nextOwnProps,
    ...getNavigationProps(nextOwnProps),
    rootProductCategory,
    selectedProduct,
    selectedCategory,
    busy: isAnyLoading(state, ['productDao', 'productCategoryDao']),
    options: getDaoOptions(state, 'productCategoryDao'),
    errors: getLoadingErrors(state, ['productDao', 'productCategoryDao']), ...nextOwnProps
  };
};

const ConnectedProductCategoryList =  withRouter(connect(mapStateToProps)(ProductCategoryList));

export default ConnectedProductCategoryList;

