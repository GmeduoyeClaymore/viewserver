import React, {Component} from 'react';
import {View, StyleSheet} from 'react-native';
import {Text, Spinner, Container, Row, Col, Content} from 'native-base';
import {LoadingScreen, PagingListView, SearchBar} from 'common/components';
import {CheckBox} from 'common/components/basic';
import {updateSubscriptionAction} from 'common/dao/DaoActions';
import {isAnyLoading, getLoadingErrors, getDaoOptions} from 'common/dao';
import {connect} from 'custom-redux';
import yup from 'yup';
import ValidationService from 'common/services/ValidationService';

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

  headerView({options: opts, search}){ return <SearchBar onChange={search} text={opts.searchText} style={{marginBottom: 15}}/>;}

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
    const {context} = this.props;
    let {selectedProductIds = []} = this.props;
    const index = selectedProductIds.indexOf(productId);
    if (!!~index){
      selectedProductIds = selectedProductIds.filter((_, idx) => idx !== index);
    } else {
      selectedProductIds = [...selectedProductIds, productId];
    }
    context.setState({selectedProductIds});
  }


  getOptions(){
    const {options, contentType} = this.props;
    return {...options, categoryId: contentType.rootProductCategory};
  }

  render(){
    const {busy, selectedProductIds = []} = this.props;

    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>No items to display</Text>;

    return busy ? <LoadingScreen text="Loading Products" /> : <Container>
      <Content keyboardShouldPersistTaps="always" padded>
        <View>
          <PagingListView
            style={styles.pagingListView}
            {...{selectedProductIds}}
            daoName='productDao'
            dataPath={['product', 'products']}
            pageSize={10}
            search={this.search}
            options={this.getOptions()}
            rowView={this.rowView}
            paginationWaitingView={Paging}
            emptyView={NoItems}
            headerView={this.headerView}
          />
        </View>
      </Content>
    </Container>;
  }
}

const styles = StyleSheet.create({
  pagingListView: {
    backgroundColor: '#FFFFFF',
    marginTop: 10
  }
});

const mapStateToProps = (state, nextOwnProps) => {
  return {
    busy: isAnyLoading(state, ['productDao']),
    options: getDaoOptions(state, 'productDao'),
    errors: getLoadingErrors(state, ['productDao']), ...nextOwnProps
  };
};

const ConnectedProductList =  connect(mapStateToProps)(ProductList);

export default ConnectedProductList;

