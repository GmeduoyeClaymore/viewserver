import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View} from 'react-native';
import {Text, Spinner, Button, Container, Header, Title, Body, Left, Content, Row, Grid, Col} from 'native-base';
import ProductListItem from './ProductListItem';
import {updateSubscriptionAction, getNavigationProps, resetSubscriptionAction} from 'common/dao';
import {PagingListView, Icon, SearchBar, ValidatingButton} from 'common/components';
import {connect} from 'custom-redux';
import yup from 'yup';
import * as ContentTypes from 'common/constants/ContentTypes';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', 'Select Product').
    personell('Select Worker').
    rubbish('Commercial or domestic waste?');
/*eslint-enable */

const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;

const headerView  = ({options: opts, search, selectedProduct}) =>
  <Col>
    <SearchBar onChange={search} text={opts.searchText} style={{marginBottom: 15}}/>
    {selectedProduct ? <Text note style={{marginBottom: 10}}>{selectedProduct !== undefined ? selectedProduct.description : null}</Text> : null}
  </Col>;

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
    const {dispatch} = this.props;
    this.search = (searchText) => {
      dispatch(updateSubscriptionAction('productDao', {searchText}));
    };
    this.search = this.search.bind(this);
    this.rowView = ({item: p, ...rest}) => {
      return (<ProductListItem key={p.productId} product={p} {...rest}/>);
    };
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
  }

  componentWillReceiveProps(nextProps) {
    ContentTypes.resolveResourceFromProps(nextProps, resourceDictionary, this);
  }

  render(){
    const {rowView, search, props, resources} = this;
    const {context, defaultOptions, history, navigationStrategy, selectedProduct} = props;
    return  <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => {
            const {parentSelectedCategory} = context.state;
            context.setState({selectedCategory: parentSelectedCategory}, () => history.replace('/Customer/Checkout/ProductCategoryList'));
          }}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{resources.PageTitle}</Title></Body>
      </Header>
      <Content padded>
        <Grid>
          <PagingListView
            ref={c => {
              this.pagingListView = c;
            }}
            daoName='productDao'
            dataPath={['product', 'products']}
            pageSize={10}
            selectedProduct={selectedProduct}
            elementContainer={Row}
            elementContainerStyle={{flexWrap: 'wrap'}}
            options={defaultOptions}
            context={context}
            navigationStrategy={navigationStrategy}
            rowView={rowView}
            search={search}
            paginationWaitingView={Paging}
            emptyView={NoItems}
            headerView={headerView}
          />
        </Grid>
      </Content>
      <ValidatingButton fullWidth paddedBottom iconRight onPress={() => navigationStrategy.next()}
        validateOnMount={true} validationSchema={yup.object(validationSchema)} model={selectedProduct}>
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
  const {dispatch} = initialProps;
  const navProps = getNavigationProps(initialProps);

  const defaultOptions = {
    categoryId: navProps.category.categoryId
  };

  const resetProducts = () => {
    dispatch(resetSubscriptionAction('productDao', defaultOptions));
  };

  const {context} = initialProps;
  const {state: contextState} = context;
  const {selectedContentType, selectedProduct} = contextState;
  return {
    ...navProps,
    selectedContentType,
    selectedProduct,
    ...initialProps,
    resetProducts,
    defaultOptions
  };
};

const ConnectedProductList =  connect(mapStateToProps)(ProductList);

export default ConnectedProductList;
