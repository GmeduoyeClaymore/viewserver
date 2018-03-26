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
    delivery('Select which size van').
    rubbish('Commercial or domestic waste?');
/*eslint-enable */

const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;

class ProductList extends Component{
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

    this.search = this.search.bind(this);
    this.goBack = this.goBack.bind(this);
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
  }

  rowView({item: p, ...rest}){
    return (<ProductListItem key={p.productId} product={p} {...rest}/>);
  }

  headerView({options: opts, search, selectedProduct}) {
    return <Col>
      <SearchBar onChange={search} text={opts.searchText} style={{marginBottom: 15}}/>
      {selectedProduct ? <Text note style={{marginBottom: 10}}>{selectedProduct !== undefined ? selectedProduct.description : null}</Text> : null}
    </Col>;
  }

  search(searchText) {
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('productDao', {searchText}));
  }

  goBack(){
    const {context, navigationStrategy} = this.props;
    const {selectedCategory, parentSelectedCategory} = context.state;
    context.setState({selectedCategory: parentSelectedCategory}, () => navigationStrategy.prev({selectedCategory, parentSelectedCategory}));
  }

  componentWillReceiveProps(nextProps) {
    ContentTypes.resolveResourceFromProps(nextProps, resourceDictionary, this);
  }

  render(){
    const {context, defaultOptions, navigationStrategy, selectedProduct} = this.props;
    return  <Container>
      <Header withButton>
        <Left>
          <Button onPress={this.goBack}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{this.resources.PageTitle}</Title></Body>
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
            rowView={this.rowView}
            search={this.search}
            paginationWaitingView={Paging}
            emptyView={NoItems}
            headerView={this.headerView}
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

ProductList.propTypes = {
  product: PropTypes.object,
  screenProps: PropTypes.object,
  navigation: PropTypes.object,
};

const validationSchema = {
  productId: yup.string().required(),
};

const mapStateToProps = (state, initialProps) => {
  const {context, dispatch} = initialProps;
  const {selectedContentType, selectedProduct, selectedCategory} = context.state;
  const navProps = getNavigationProps(initialProps);

  const defaultOptions = {
    categoryId: navProps.parentSelectedCategory != undefined ? navProps.parentSelectedCategory.categoryId : selectedCategory.categoryId
  };

  const resetProducts = () => {
    dispatch(resetSubscriptionAction('productDao', defaultOptions));
  };

  return {
    ...navProps,
    selectedContentType,
    selectedProduct,
    resetProducts,
    defaultOptions,
    ...initialProps
  };
};

const ConnectedProductList =  connect(mapStateToProps)(ProductList);
export default ConnectedProductList;
