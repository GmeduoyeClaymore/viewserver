import React, {Component} from 'react';
import {View, Text, Spinner, Row, Button} from 'native-base';
import {LoadingScreen, PagingListView, Icon} from 'common/components';
import {isAnyLoading, getLoadingErrors, getDaoOptions} from 'common/dao';
import {withExternalState} from 'custom-redux';
import yup from 'yup';
import ValidationService from 'common/services/ValidationService';
import shotgun from 'native-base-theme/variables/shotgun';

class ProductSelector extends Component{
  rowView = ({item, index: i, selectedProductIds}) => {
    const {productId, name} = item;
    const isSelected = !!~selectedProductIds.indexOf(productId);

    return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
      <Button style={{height: 'auto'}} large active={isSelected} onPress={() => this.toggleProduct(productId)}>
        <Icon name={productId}/>
      </Button>
      <Text style={styles.productSelectText}>{name}</Text>
    </View>;
  }

  toggleProduct = (productId) => {
    let {selectedProductIds = []} = this.props;
    const index = selectedProductIds.indexOf(productId);
    if (!!~index){
      selectedProductIds = selectedProductIds.filter((_, idx) => idx !== index);
    } else {
      selectedProductIds = [...selectedProductIds, productId];
    }
    this.setState({selectedProductIds});
  }

  render(){
    const {busy, selectedProductIds = [], options, contentType} = this.props;

    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>No items to display</Text>;

    return busy ? <LoadingScreen text="Loading Products" /> : <PagingListView
      style={styles.pagingListView}
      {...{selectedProductIds}}
      daoName='productDao'
      dataPath={['product', 'products']}
      pageSize={20}
      elementContainer={Row}
      elementContainerStyle={{flexWrap: 'wrap'}}
      options={{...options, categoryId: contentType.rootProductCategory}}
      rowView={this.rowView}
      paginationWaitingView={Paging}
      emptyView={NoItems}
      headerView={undefined}
    />;
  }
}

const validationSchema = {
  selectedProductIds: yup.array().required()
};

const styles = {
  pagingListView: {
    backgroundColor: shotgun.brandPrimary,
    marginTop: 20
  },
  productSelectText: {
    width: '100%',
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  }
};

const mapStateToProps = (state, nextOwnProps) => {
  return {
    busy: isAnyLoading(state, ['productDao']),
    options: getDaoOptions(state, 'productDao'),
    errors: getLoadingErrors(state, ['productDao']), ...nextOwnProps
  };
};

const canSubmit = async (state, user) => {
  const {selectedProductIds} = state;

  return  user !== undefined ? undefined : await ValidationService.validate({selectedProductIds}, yup.object(validationSchema));
};

const ConnectedProductList =  withExternalState(mapStateToProps)(ProductSelector);
export default {control: ConnectedProductList, validator: canSubmit};
