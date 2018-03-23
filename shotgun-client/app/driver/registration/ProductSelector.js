import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Spinner, Container, Row, Col, Content} from 'native-base';
import {LoadingScreen, PagingListView} from 'common/components';
import {CheckBox} from 'common/components/basic';
import {isAnyLoading, getLoadingErrors, getDaoOptions} from 'common/dao';
import {connect} from 'custom-redux';
import yup from 'yup';
import ValidationService from 'common/services/ValidationService';
import shotgun from 'native-base-theme/variables/shotgun';

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
    this.renderSelectionControl = this.renderSelectionControl.bind(this);
  }

  renderSelectionControl = ({productId, selectedProductIds}) => {
    const checked = !!~selectedProductIds.indexOf(productId);
    return <CheckBox  key={productId} onPress={() => this.toggleProduct(productId)} categorySelectionCheckbox checked={checked}/>;
  }

  rowView({item: row, selectedProductIds}){
    const {productId, name, description} = row;
    const {renderSelectionControl: SelectionControl} = this;
    return <Row key={productId} style={styles.productRow}>
      <Col style={styles.selectionControlColumn}>
        <SelectionControl productId={productId} selectedProductIds={selectedProductIds}/>
      </Col>
      <Col>
        <Text>{name}</Text>
        <Text style={styles.productDescription}>{description}</Text>
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

  render(){
    const {busy, selectedProductIds = [], options, contentType} = this.props;

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
            options={{...options, categoryId: contentType.rootProductCategory}}
            rowView={this.rowView}
            paginationWaitingView={Paging}
            emptyView={NoItems}
            headerView={undefined}
          />
        </View>
      </Content>
    </Container>;
  }
}

const styles = {
  pagingListView: {
    backgroundColor: shotgun.brandPrimary,
    paddingTop: 10
  },
  productRow: {
    padding: 5,
    paddingTop: 20,
    backgroundColor: shotgun.brandPrimary
  },
  selectionControlColumn: {
    width: 50,
    paddingTop: 5,
    marginRight: 10
  },
  productDescription: {
    color: shotgun.brandLight
  }
};

const mapStateToProps = (state, nextOwnProps) => {
  return {
    busy: isAnyLoading(state, ['productDao']),
    options: getDaoOptions(state, 'productDao'),
    errors: getLoadingErrors(state, ['productDao']), ...nextOwnProps
  };
};

const ConnectedProductList =  connect(mapStateToProps)(ProductList);
export default ConnectedProductList;

