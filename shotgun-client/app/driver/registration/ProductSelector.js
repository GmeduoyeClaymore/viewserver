import React, {Component} from 'react';
import {View, StyleSheet, Image} from 'react-native';
import {Text, Spinner, Container, Row, Col, Content, Item} from 'native-base';
import {LoadingScreen, PagingListView, SearchBar, Icon} from 'common/components';
import {CheckBox} from 'common/components/basic';
import {updateSubscriptionAction} from 'common/dao/DaoActions';
import {isAnyLoading, getLoadingErrors, getDaoOptions} from 'common/dao';
import {connect} from 'custom-redux';
import yup from 'yup';
import ValidationService from 'common/services/ValidationService';
import {resolveProductCategoryIcon} from 'common/assets';

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    marginTop: 10
  },
  subTitle: {
    marginTop: 25,
    marginBottom: 30
  },
  picture: {
    height: 40,
    width: 40,
    marginLeft: 10
  }
});

const headerView  = ({options: opts, search}) => <SearchBar onChange={search} text={opts.searchText} style={{marginBottom: 15}}/>;

class ProductList extends Component{
  static validationSchema = {
    selectedProductIds: yup.array().required()
  };

  static async canSubmit(state){
    const {selectedProductIds} = state;
    return await ValidationService.validate({selectedProductIds}, yup.object(ProductList.validationSchema));
  }

  constructor(props){
    super(props);
    this.rowView = this.rowView.bind(this);
    const {dispatch} = this.props;
    this.search = (searchText) => {
      dispatch(updateSubscriptionAction('productDao', {searchText}));
    };
    this.search = this.search.bind(this);
    this.renderSelectionControl = this.renderSelectionControl.bind(this);
  }

  renderSelectionControl = ({productId, selectedProductIds}) => {
    const checked = !!~selectedProductIds.indexOf(productId);
    return <CheckBox  key={productId} onPress={() => this.toggleProduct(productId)} categorySelectionCheckbox checked={checked}/>;
  }

  rowView({item: row, selectedProductIds}){
    const {productId, name, description} = row;
    const {renderSelectionControl: SelectionControl} = this;
    return <Row key={productId} style={{flexDirection: 'row', flex: 1, padding: 5, backgroundColor: 'white'}}>
      <View style={{width: 50, paddingTop: 25}}>
        <SelectionControl productId={productId} selectedProductIds={selectedProductIds}/>
      </View>
      <Col style={{padding: 10, paddingTop: 20, flex: 1}}>
        <Text >{`${name}`}</Text>
        <Text >{`${description}`}</Text>
      </Col>
    </Row>;
  }

  toggleProduct(productId){
    let {context, selectedProductIds = []} = this.props;
    const index = selectedProductIds.indexOf(productId);
    if (!!~index){
      selectedProductIds = selectedProductIds.filter((_, idx) => idx !== index);
    } else {
      selectedProductIds = [...selectedProductIds, productId];
    }
    context.setState({selectedProductIds});
  }


  getOptions(){
    const {options, selectedProductIds, contentType} = this.props;
    return {...options, categoryId: contentType.rootProductCategory};
  }

  render(){
    const {busy, selectedProductIds = []} = this.props;
    const {rowView, search} = this;

    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>No items to display</Text>;

    return busy ? <LoadingScreen text="Loading Products" /> : <Container>
      <Content keyboardShouldPersistTaps="always" padded>
        <View>
          <PagingListView
            style={styles.container}
            {...{selectedProductIds}}
            daoName='productDao'
            dataPath={['product', 'products']}
            pageSize={10}
            search={search}
            options={this.getOptions()}
            rowView={rowView}
            paginationWaitingView={Paging}
            emptyView={NoItems}
            headerView={headerView}
          />
        </View>
      </Content>
    </Container>;
  }
}

const mapStateToProps = (state, nextOwnProps) => {
  return {
    busy: isAnyLoading(state, ['productDao']),
    options: getDaoOptions(state, 'productDao'),
    errors: getLoadingErrors(state, ['productDao']), ...nextOwnProps
  };
};

const ConnectedProductList =  connect(mapStateToProps)(ProductList);

export default ConnectedProductList;

