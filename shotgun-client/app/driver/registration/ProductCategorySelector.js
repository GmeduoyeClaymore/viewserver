import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Spinner, Row, Content, Item} from 'native-base';
import {LoadingScreen, PagingListView, SearchBar, Icon} from 'common/components';
import {updateSubscriptionAction} from 'common/dao/DaoActions';
import {CheckBox} from 'common/components/basic';
import {isAnyLoading, getLoadingErrors, getDaoOptions} from 'common/dao';
import {connect} from 'custom-redux';
import yup from 'yup';
import ValidationService from 'common/services/ValidationService';
import shotgun from 'native-base-theme/variables/shotgun';

class ProductCategoryList extends Component {
  static validationSchema = {
    selectedProductCategories: yup.array().required()
  };

  static async canSubmit(state) {
    const {selectedProductCategories} = state;
    return await ValidationService.validate({selectedProductCategories}, yup.object(ProductCategoryList.validationSchema));
  }

  constructor(props) {
    super(props);
    this.rowView = this.rowView.bind(this);
    const {dispatch} = this.props;
    this.search = (searchText) => {
      dispatch(updateSubscriptionAction('productCategoryDao', {searchText}));
    };
    this.search = this.search.bind(this);
    this.renderExpanderControl = this.renderExpanderControl.bind(this);
    this.renderSelectionControl = this.renderSelectionControl.bind(this);
  }

  isImplicitlyChecked(categoryObj, selectedProductCategories) {
    return !!selectedProductCategories.find(c => this.isDescendendantOf(categoryObj, c));
  }

  isDescendendantOf(parent, child) {
    return child.path.includes(parent.path + '>') && child.path.length > parent.path.length;
  }

  renderExpanderControl = ({categoryId, expandedCategoryIds}) => {
    const baseIconStyle = {color: '#edeaea', borderBottomWidth: 0};
    const expandedStyle = {transform: [{rotate: '90deg'}], marginTop: 15};
    const iconStyle = !!~expandedCategoryIds.indexOf(categoryId) ? expandedStyle : {};

    return <Item key={categoryId} style={{width: 25, height: 25, borderBottomWidth: 0}} onPress={() => this.expandCategory(categoryId)}>
      <Icon name='forward-arrow' style={{...baseIconStyle, ...iconStyle}}/>
    </Item>;
  }

  renderSelectionControl = ({categoryObj, selectedProductCategories = []}) => {
    const checked = this.isChecked(categoryObj);
    const implicitylChecked = this.isImplicitlyChecked(categoryObj, selectedProductCategories);
    return <CheckBox key={categoryObj.categoryId} onPress={() => this.toggleCategory(categoryObj)} categorySelectionCheckbox checked={checked} implicitylChecked={implicitylChecked}/>;
  }

  isChecked(categoryObj) {
    const {selectedProductCategories = []} = this.props;
    const isExplicitlyChecked = !!selectedProductCategories.find(c => c.categoryId === categoryObj.categoryId);
    return isExplicitlyChecked && !this.isImplicitlyChecked(categoryObj, selectedProductCategories);
  }

  rowView({item: row, selectedProductCategories, expandedCategoryIds}) {
    const {categoryId, category} = row;
    const {renderSelectionControl: SelectionControl} = this;
    const {renderExpanderControl: ExpanderControl} = this;
    const {contentType} = this.props;
    const {productCategory} = contentType;
    const level = (row.level - productCategory.level) - 1;
    return <Row key={categoryId} style={{flexDirection: 'row', flex: 1, padding: 5, backgroundColor: 'white'}}>
      <View style={{width: 50, paddingTop: 10, marginLeft: level * 20}}>
        <SelectionControl categoryObj={row} selectedProductCategories={selectedProductCategories}/>
      </View>

      <Text style={{padding: 10, paddingTop: 20, flex: 1}}>{`${category}`}</Text>
      <View style={{paddingTop: 20, width: 50}}>
        {row.isLeaf ? null : <ExpanderControl categoryId={categoryId} expandedCategoryIds={expandedCategoryIds}/>}
      </View>
    </Row>;
  }

  headerView({options: opts, search}) {
    return <SearchBar onChange={search} text={opts.searchText} style={{marginBottom: 15}}/>;
  }

  toggleCategory(categoryObj) {
    const {context} = this.props;
    let {selectedProductCategories = []} = this.props;
    const index = selectedProductCategories.findIndex(c => c.categoryId === categoryObj.categoryId);
    if (!!~index || this.isImplicitlyChecked(categoryObj, selectedProductCategories)) {
      selectedProductCategories = selectedProductCategories.filter((category, idx) => idx !== index);
      selectedProductCategories = selectedProductCategories.filter((category) => !this.isDescendendantOf(categoryObj, category));
    } else {
      selectedProductCategories = [...selectedProductCategories, categoryObj];
    }
    context.setState({selectedProductCategories});
  }

  expandCategory(categoryId) {
    const {context} = this.props;
    let {expandedCategoryIds = []} = this.props;
    const index = expandedCategoryIds.indexOf(categoryId);
    if (!!~index) {
      expandedCategoryIds = expandedCategoryIds.filter((_, idx) => idx !== index);
    } else {
      expandedCategoryIds = [...expandedCategoryIds, categoryId];
    }
    context.setState({expandedCategoryIds});
  }

  getOptions() {
    const {options, expandedCategoryIds, contentType} = this.props;
    return {...options, parentCategoryId: contentType.rootProductCategory, expandedCategoryIds};
  }

  render() {
    const {busy, selectedProductCategories = [], expandedCategoryIds = [], options, hideSearchBar} = this.props;

    const Paging = () => <Spinner/>;
    const NoItems = () => <Text empty>No items to display</Text>;
    return busy ? <LoadingScreen text="Loading Product Categories"/> :
      <Content keyboardShouldPersistTaps="always" style={styles.content} padded>
        <View style={styles.view}>
          <PagingListView
            style={styles.pagingListView}
            {...{selectedProductCategories, expandedCategoryIds}}
            daoName='productCategoryDao'
            dataPath={['product', 'categories']}
            pageSize={10}
            search={this.search}
            value={options.searchText}
            options={this.getOptions()}
            rowView={this.rowView}
            paginationWaitingView={Paging}
            emptyView={NoItems}
            headerView={hideSearchBar ? () => null : this.headerView}
          />
        </View>
      </Content>;
  }
}

const styles = {
  content: {
    flex: 1,
    marginTop: 10
  },
  view: {
    flex: 1,
    height: shotgun.deviceHeight - 220
  },
  pagingListView: {
    backgroundColor: shotgun.brandPrimary,
    marginTop: 10
  }
};

const mapStateToProps = (state, nextOwnProps) => {
  return {
    busy: isAnyLoading(state, ['productCategoryDao']),
    options: getDaoOptions(state, 'productCategoryDao'),
    errors: getLoadingErrors(state, ['productCategoryDao']), ...nextOwnProps
  };
};

const ConnectedProductCategoryList = connect(mapStateToProps)(ProductCategoryList);
export default ConnectedProductCategoryList;

