import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Spinner, Row, Button} from 'native-base';
import {LoadingScreen, PagingListView, Icon} from 'common/components';
import {isAnyLoading, getLoadingErrors, getDaoOptions, getDaoState} from 'common/dao';
import {withExternalState} from 'custom-redux';
import yup from 'yup';
import ValidationService from 'common/services/ValidationService';
import shotgun from 'native-base-theme/variables/shotgun';

class ProductCategorySelector extends Component {
  rowView = ({item: row, selectedProductCategories}) => {
    const {categoryId, category} = row;
    const isSelected = !!selectedProductCategories.find(c => c.categoryId === categoryId);

    return <View key={categoryId} style={{width: '50%', paddingRight: 5, paddingLeft: 5, maxWidth: 250, maxHeight: 250}}>
      <Button style={{height: 'auto'}} large active={isSelected}  onPress={() => this.toggleCategory(row)}>
        <Icon name={categoryId}/>
      </Button>
      <Text style={styles.categoryText}>{category}</Text>
    </View>;
  }

  toggleCategory = (row) => {
    const {path, categoryId} = row;
    const categoryObj = {path, categoryId};
    let {selectedProductCategories = []} = this.props;

    const index = selectedProductCategories.findIndex(c => c.categoryId === categoryObj.categoryId);
    if (!!~index) {
      selectedProductCategories = selectedProductCategories.filter((category, idx) => idx !== index);
    } else {
      selectedProductCategories = [...selectedProductCategories, categoryObj];
    }

    this.setState({selectedProductCategories});
  }

  getOptions = () => {
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
        elementContainer={Row}
        elementContainerStyle={{flexWrap: 'wrap'}}
        pageSize={10}
        options={this.getOptions()}
        rowView={this.rowView}
        paginationWaitingView={Paging}
        emptyView={NoItems}
        headerView={undefined}
      />;
  }
}

const validationSchema = {
  selectedProductCategories: yup.array().required()
};


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

const canSubmit = async (state, user) => {
  const {selectedProductCategories} = state;

  return user !== undefined ? undefined : await ValidationService.validate({selectedProductCategories}, yup.object(validationSchema));
};

const ConnectedProductCategoryList = withExternalState(mapStateToProps)(ProductCategorySelector);
export default {control: ConnectedProductCategoryList, validator: canSubmit};

