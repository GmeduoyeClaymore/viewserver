import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet} from 'react-native';
import {Text, Spinner, Button, Container, Header, Title, Body, Left, Content, Row} from 'native-base';
import { withRouter } from 'react-router';
import {LoadingScreen, PagingListView, ValidatingButton, Icon} from 'common/components';
import {connect} from 'custom-redux';
import {Redirect} from 'react-router-native';
import ProductListItem from './ProductListItem';
import yup from 'yup';
import * as ContentTypes from 'common/constants/ContentTypes';
import {mapStateToProps, validationSchema} from './ProductCategoryList';
/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', 'Select Category').
    personell('Select Worker').
    rubbish('Commercial or domestic waste?');
/*eslint-enable */

const headerView  = ({selectedCategory}) => (selectedCategory ? <Text note style={{marginBottom: 10}}>{selectedCategory !== undefined ? selectedCategory.description : null}</Text> : null);

class FlatProductCategoryList extends Component{
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
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
  }

  componentWillReceiveProps(nextProps) {
    ContentTypes.resolveResourceFromProps(nextProps, resourceDictionary, this);
  }

  rowView({item: row, index: i, selectedCategory}){
    const {categoryId, category, imageUrl} = row;
 
    return <View key={categoryId} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
      <Button style={{height: 'auto'}} large active={selectedCategory.categoryId == row.categoryId} onPress={() => this.navigateToCategory(row)}>
        <Icon name={imageUrl || 'dashed'}/>
      </Button>
      <Text style={styles.productSelectText}>{category}</Text>
    </View>;
  }


  navigateToCategory(category){
    const {history} = this.props;
    if (category.isLeaf) {
      history.push('/Customer/Checkout/ProductList', {category});
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
    const {rowView, resources} = this;

    if (selectedCategory.isLeaf){
      return <Redirect push={true} to={{pathname: '/Customer/Checkout/ProductList', state: {category: selectedCategory}}}/>;
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
        <Body><Title>{resources.PageTitle}</Title></Body>
      </Header>
      <Content padded>
        <ProductListItem product={selectedProduct}/>
        <PagingListView
          style={styles.container}
          selectedCategory={selectedCategory}
          elementContainer={Row}
          elementContainerStyle={{flexWrap: 'wrap'}}
          daoName='productCategoryDao'
          dataPath={['product', 'categories']}
          pageSize={10}
          options={defaultOptions}
          history={history}
          rowView={rowView}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          headerView={headerView}
        />
      </Content>
      <ValidatingButton fullWidth paddedBottom iconRight onPress={() =>  navigationStrategy.next()} validateOnMount={true} validationSchema={yup.object(validationSchema)} model={selectedProduct}>
        <Text uppercase={false}>Continue</Text>
        <Icon next name='forward-arrow'/>
      </ValidatingButton>
    </Container>;
  }
}


const styles = {
  subTitle: {
    marginTop: 25,
    marginBottom: 30,
    fontSize: 13
  },
  productSelectText: {
    width: '100%',
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  },
  container: {
    backgroundColor: '#FFFFFF',
    marginTop: 10
  },
  separator: {
    height: StyleSheet.hairlineWidth,
  }
};

const ConnectedProductCategoryList =  withRouter(connect(mapStateToProps)(FlatProductCategoryList));

export default ConnectedProductCategoryList;

