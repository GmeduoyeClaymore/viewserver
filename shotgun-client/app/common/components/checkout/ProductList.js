import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View} from 'react-native';
import {Text, Spinner, Button, Container, Header, Title, Body, Left, Content, Row, Grid, Col} from 'native-base';
import ProductListItem from './ProductListItem';
import {updateSubscriptionAction, getNavigationProps} from 'common/dao';
import {PagingListView, Icon, SearchBar, ValidatingButton} from 'common/components';
import {withExternalState} from 'custom-redux';
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
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
  }

  componentWillReceiveProps(nextProps) {
    ContentTypes.resolveResourceFromProps(nextProps, resourceDictionary, this);
  }

  rowView({item: p, ...rest}){
    return (<ProductListItem key={p.productId} product={p} {...rest}/>);
  }

  headerView({options, search, selectedProduct, size}) {
    return size > 20 ? <Col>
      <SearchBar onChange={search} text={options.searchText} style={{marginBottom: 15}}/>
      {selectedProduct ? <Text note style={{marginBottom: 10}}>{selectedProduct !== undefined ? selectedProduct.description : null}</Text> : null}
    </Col> : null;
  }

  search = (searchText) => {
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('productDao', {searchText}));
  }

  goBack = () => {
    const {parentSelectedCategory, history} = this.props;
    history.goBack();
    this.setState({selectedCategory: parentSelectedCategory});
  }

  render(){
    const {defaultOptions, next, selectedProduct, stateKey, history} = this.props;
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
            daoName='productDao'
            dataPath={['product', 'products']}
            pageSize={10}
            selectedProduct={selectedProduct}
            elementContainer={Row}
            elementContainerStyle={{flexWrap: 'wrap'}}
            options={defaultOptions}
            stateKey={stateKey}
            next={next}
            history={history}
            rowView={this.rowView}
            search={this.search}
            setState={this.setState}
            paginationWaitingView={Paging}
            emptyView={NoItems}
            headerView={this.headerView}
          />
        </Grid>
      </Content>
      <ValidatingButton fullWidth paddedBottom iconRight onPress={() => history.push(next)}
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
  const {selectedContentType, selectedProduct, selectedCategory = {}} = initialProps;
  const navProps = getNavigationProps(initialProps);

  const defaultOptions = {
    categoryId: navProps.parentSelectedCategory != undefined ? navProps.parentSelectedCategory.categoryId : selectedCategory.categoryId
  };

  return {
    ...navProps,
    selectedContentType,
    selectedProduct,
    defaultOptions,
    ...initialProps
  };
};

export default withExternalState(mapStateToProps)(ProductList);
