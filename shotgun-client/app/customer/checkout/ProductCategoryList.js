import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, Text, TouchableHighlight} from 'react-native';
import {Icon, Button, Container, Header, Title, Body, Left, Content} from 'native-base';
import {Spinner} from 'native-base';
import { withRouter } from 'react-router';
import PagingListView from 'common/components/PagingListView';
import {updateSubscriptionAction, isAnyLoading, getLoadingErrors, getDaoOptions, getNavigationProps} from 'common/dao';
import {connect} from 'custom-redux';
import yup from 'yup';
import ErrorRegion from 'common/components/ErrorRegion';
import ValidatingButton from 'common/components/ValidatingButton';

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

const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;


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

  rowView(row){
    const {categoryId, category} = row;
    return <TouchableHighlight key={categoryId} style={{flex: 1, flexDirection: 'row'}} onPress={() => this.navigateToCategory({category: row})} underlayColor={'#EEEEEE'}>
      <View style={{flexDirection: 'column', flex: 1, padding: 0}}>
        <Text>{`${category}`}</Text>
      </View>
    </TouchableHighlight>;
  }

  navigateToCategory({category}){
    const {history, context} = this.props;
    const {selectedCategory = {}} = context.state;
    if (selectedCategory.isLeaf) {
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
    const {busy, errors, options, navigationStrategy, selectedProduct, selectedCategory, history, rootProductCategory} = this.props;
    const {rowView} = this;

    return busy ? <LoadingScreen text="Loading Product Categories" /> : <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='arrow-back' onPress={() => navigationStrategy.prev()} />
          </Button>
        </Left>
        <Body><Title>Select Product Category</Title></Body>
      </Header>
      <Content padded>
        <Text style={styles.subTitle}>Selected product is {JSON.stringify(selectedProduct)}</Text>
        <ErrorRegion errors={errors}>
          <PagingListView
            style={styles.container}
            daoName='productCategoryDao'
            dataPath={['product', 'categories']}
            pageSize={10}
            options={{...options, parentCategoryId: selectedCategory ? selectedCategory.categoryId : rootProductCategory}}
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
  const {rootProductCategory} = selectedContentType;
  return {
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

