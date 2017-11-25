import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, TouchableHighlight} from 'react-native';
import {Text, Content, Header, Left, Body, Container, Button, Icon, Title} from 'native-base';
import {Spinner} from 'native-base';
import PagingListView from 'common/components/PagingListView';
import {isAnyLoading, getLoadingErrors, getDaoOptions, updateSubscriptionAction} from 'common/dao';
import {connect} from 'react-redux';
import ErrorRegion from 'common/components/ErrorRegion';

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    marginTop: 10
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    height: 30
  }
});

const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;

class ProductCategoryList extends Component{
  static propTypes = {
    product: PropTypes.object,
    dispatch: PropTypes.func
  };

  navFuncFactory({categoryId, category, isLeaf}) {
    const {history, match} = this.props;

    if (isLeaf == 'true') { //TODO - get isLeaf to be a proper bool in the ViewServer
      history.push(`${match.path}/ProductList`, {categoryId, category});
    } else {
      history.push(`${match.path}/ProductCategoryList`, {parentCategoryId: categoryId, parentCategory: category});
    }
  }
  constructor(props){
    super(props);
    this.rowView = (row) => {
      const {categoryId, category, isLeaf} = row;
      return <TouchableHighlight key={categoryId} style={{flex: 1, flexDirection: 'row'}} onPress={() => this.navFuncFactory({categoryId, category, isLeaf})} underlayColor={'#EEEEEE'}>
        <View style={{flexDirection: 'column', flex: 1, padding: 0}}>
          <Text>{`${category}`}</Text>
        </View>
      </TouchableHighlight>;
    };
  }

  componentDidMount(){
    this.updateSubs('NONE');
  }

  componentWillReceiveProps(nextProps){
    const {state = {}} = nextProps.location;
    //TODO - get rid of NONE and return it as undefined from the viewserver
    const {parentCategoryId = 'NONE'} = state;

    this.updateSubs(parentCategoryId);
  }

  updateSubs(parentCategoryId){
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('productCategoryDao', {parentCategoryId}));
  }

  render(){
    const {busy, errors, options, history, location} = this.props;
    const {state = {}} = location;
    const {parentCategory} = state;
    const {rowView} = this;
    return busy ?  <Container><Paging/></Container> :
      <Container>
        {parentCategory ? <Header>
          <Left>
            <Button transparent>
              <Icon name='arrow-back' onPress={() => history.goBack()} />
            </Button>
          </Left>
          <Body><Title>{parentCategory}</Title></Body>
        </Header> : null}
        <Content>
          <ErrorRegion errors={errors}>
            <PagingListView
              style={styles.container}
              daoName='productCategoryDao'
              dataPath={['product', 'categories']}
              pageSize={10}
              options={options}
              rowView={rowView}
              paginationWaitingView={Paging}
              emptyView={NoItems}
              headerView={() => null}/>
          </ErrorRegion>
        </Content>
      </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isAnyLoading(state, ['productDao', 'productCategoryDao']),
  options: getDaoOptions(state, 'productCategoryDao'),
  errors: getLoadingErrors(state, ['productDao', 'productCategoryDao']), ...nextOwnProps
});

const ConnectedProductCategoryList = connect(mapStateToProps)(ProductCategoryList);

export default ConnectedProductCategoryList;

