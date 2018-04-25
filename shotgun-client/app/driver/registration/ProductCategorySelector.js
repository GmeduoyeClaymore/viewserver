import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Spinner, Row} from 'native-base';
import {LoadingScreen, PagingListView, CheckBox} from 'common/components';
import {isAnyLoading, getLoadingErrors, getDaoOptions} from 'common/dao';
import {withExternalState} from 'custom-redux';
import yup from 'yup';
import ValidationService from 'common/services/ValidationService';
import shotgun from 'native-base-theme/variables/shotgun';

class ProductCategoryList extends Component {
  static validationSchema = {
    selectedProductCategories: yup.array().required()
  };
  

  constructor(props) {
    super(props);
    this.rowView = this.rowView.bind(this);
    this.renderSelectionControl = this.renderSelectionControl.bind(this);
  }

  isImplicitlyChecked(categoryObj, selectedProductCategories) {
    return !!selectedProductCategories.find(c => this.isDescendendantOf(categoryObj, c));
  }

  isDescendendantOf(parent, child) {
    return child.path.includes(parent.path + '>') && child.path.length > parent.path.length;
  }

  renderSelectionControl = ({categoryObj, selectedProductCategories = [], context}) => {
    const checked = context.isChecked(categoryObj);
    const implicitlyChecked = context.isImplicitlyChecked(categoryObj, selectedProductCategories);
    return <CheckBox style={{left: 10}} key={categoryObj.categoryId} onPress={() => context.toggleCategory(categoryObj)} categorySelectionCheckbox checked={checked} implicitylChecked={implicitlyChecked}/>;
  }

  isChecked(categoryObj) {
    const {selectedProductCategories = []} = this.props;
    const isExplicitlyChecked = !!selectedProductCategories.find(c => c.categoryId === categoryObj.categoryId);
    return isExplicitlyChecked && !this.isImplicitlyChecked(categoryObj, selectedProductCategories);
  }

  rowView({item: row, selectedProductCategories}) {
    const {categoryId, category} = row;
    const {renderSelectionControl: SelectionControl} = this;

    return <Row key={categoryId} style={styles.categoryRow}>
      <View style={styles.categoryView}>
        <SelectionControl categoryObj={row} selectedProductCategories={selectedProductCategories} context={this}/>
      </View>
      <Text style={styles.categoryText}>{category}</Text>
    </Row>;
  }

  toggleCategory(categoryObj) {
    let {selectedProductCategories = []} = this.props;
    const index = selectedProductCategories.findIndex(c => c.categoryId === categoryObj.categoryId);
    if (!!~index || this.isImplicitlyChecked(categoryObj, selectedProductCategories)) {
      selectedProductCategories = selectedProductCategories.filter((category, idx) => idx !== index);
      selectedProductCategories = selectedProductCategories.filter((category) => !this.isDescendendantOf(categoryObj, category));
    } else {
      selectedProductCategories = [...selectedProductCategories, categoryObj];
    }
    this.setState({selectedProductCategories});
  }

  expandCategory(categoryId) {
    let {expandedCategoryIds = []} = this.props;
    const index = expandedCategoryIds.indexOf(categoryId);
    if (!!~index) {
      expandedCategoryIds = expandedCategoryIds.filter((_, idx) => idx !== index);
    } else {
      expandedCategoryIds = [...expandedCategoryIds, categoryId];
    }
    this.setState({expandedCategoryIds});
  }

  getOptions() {
    const {options, expandedCategoryIds, contentType} = this.props;
    return {...options, parentCategoryId: contentType.rootProductCategory, expandedCategoryIds};
  }

  render() {
    const {busy, selectedProductCategories = [], expandedCategoryIds = []} = this.props;

    const Paging = () => <Spinner/>;
    const NoItems = () => <Text empty>No items to display</Text>;
    return busy ? <LoadingScreen text="Loading Product Categories"/> :
      <PagingListView
        style={styles.pagingListView}
        {...{selectedProductCategories, expandedCategoryIds}}
        daoName='productCategoryDao'
        dataPath={['product', 'categories']}
        pageSize={10}
        options={this.getOptions()}
        rowView={this.rowView}
        paginationWaitingView={Paging}
        emptyView={NoItems}
        headerView={undefined}
      />;
  }
}

const styles = {
  pagingListView: {
    backgroundColor: shotgun.brandPrimary,
    paddingTop: 10
  },
  categoryRow: {
    backgroundColor: shotgun.brandPrimary
  },
  categoryView: {
    width: 50,
    paddingTop: 10
  },
  categoryText: {
    padding: 10,
    paddingTop: 20
  }
};

const mapStateToProps = (state, nextOwnProps) => {
  return {
    busy: isAnyLoading(state, ['productCategoryDao']),
    options: getDaoOptions(state, 'productCategoryDao'),
    errors: getLoadingErrors(state, ['productCategoryDao']), ...nextOwnProps
  };
};

const  canSubmit = async (state) => {
  const {selectedProductCategories} = state;
  return await ValidationService.validate({selectedProductCategories}, yup.object(ProductCategoryList.validationSchema));
};

const ConnectedProductCategoryList = withExternalState(mapStateToProps)(ProductCategoryList);
export default {control: ConnectedProductCategoryList, validator: canSubmit};

