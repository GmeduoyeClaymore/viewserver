import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, Text} from 'react-native';
import {Spinner, Button, Container, Header, Title, Body, Left, Content} from 'native-base';
import ProductListItem from './ProductListItem';
import {updateSubscriptionAction, getNavigationProps} from 'common/dao';
import {LoadingScreen, PagingListView, Icon, SearchBar} from 'common/components';
import {connect} from 'custom-redux';

const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;

const headerView  = ({options: opts, search}) => <SearchBar onChange={search} text={opts.searchText} style={{marginBottom: 15}}/>;
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
  }

  render(){
    const {rowView, search, props} = this;
    const {context, category = {}, navigationStrategy, busy} = props;
    return   busy ? <LoadingScreen text="Loading Products...." /> : <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='back-arrow' onPress={() => navigationStrategy.prev()} />
          </Button>
        </Left>
        <Body><Title>Select Product</Title></Body>
      </Header>
      <Content padded>
        <PagingListView
          daoName='productDao'
          dataPath={['product', 'products']}
          pageSize={10}
          options={{categoryId: category.categoryId}}
          context={context}
          navigationStrategy={navigationStrategy}
          rowView={rowView}
          search={search}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          headerView={headerView}
        />
      </Content>
    </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  const navProps = getNavigationProps(nextOwnProps);
  return {
    ...navProps,
    ...nextOwnProps
  };
};

const ConnectedProductList =  connect(mapStateToProps)(ProductList);

export default ConnectedProductList;
