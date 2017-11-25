import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet} from 'react-native';
import SearchBar from './SearchBar';
import ProductListItem from './ProductListItem';
import {Spinner, Text, Content, Header, Left, Body, Container, Button, Icon, Title} from 'native-base';
import {updateSubscriptionAction, isAnyLoading, getLoadingErrors, getDaoOptions} from 'common/dao';
import PagingListView from '../../common/components/PagingListView';
import {connect} from 'react-redux';

const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    marginTop: 10
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
});

class ProductList extends Component{
  static propTypes = {
    product: PropTypes.object,
    screenProps: PropTypes.object,
    navigation: PropTypes.object,
  };


  constructor(props){
    super(props);
    this.handleSearch = this.handleSearch.bind(this);
    this.rowView = (p) => {
      return (<ProductListItem key={p.productId} product={p}/>);
    };
    this.renderSearchBar = () => <SearchBar onChange={this.handleSearch} />;
  }

  handleSearch(searchText){
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('productDao', {searchText}));
  }

  componentDidMount(){
    this.updateSubs();
  }

  updateSubs(){
    const {location, dispatch} = this.props;
    const {state = {}} = location;
    const {categoryId} = state;
    dispatch(updateSubscriptionAction('productDao', {categoryId}));
  }

  render(){
    const {rowView} = this;
    const {location, history} = this.props;
    const {state = {}} = location;
    const {category} = state;

    return <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>{category}</Title></Body>
      </Header>
      <Content>
        <PagingListView
          style={styles.container}
          daoName='productDao'
          dataPath={['product', 'products']}
          pageSize={10}
          rowView={rowView}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          headerView={this.renderSearchBar}
        />
      </Content>
    </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['productDao']),
  options: getDaoOptions(state, 'productDao'),
  errors: getLoadingErrors(state, ['productDao']), ...nextOwnProps
});

const ConnectedProductList =  connect(mapStateToProps)(ProductList);

export default ConnectedProductList;
