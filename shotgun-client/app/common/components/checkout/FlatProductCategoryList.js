import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View, Image} from 'react-native';
import {Text, Spinner, Button, Container, Header, Title, Body, Left, Content, Row} from 'native-base';
import {LoadingScreen, PagingListView, ValidatingButton, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';
import yup from 'yup';
import * as ContentTypes from 'common/constants/ContentTypes';
import shotgun from 'native-base-theme/variables/shotgun';
import {isAnyLoading, getLoadingErrors, getDaoOptions, getNavigationProps, getDaoState} from 'common/dao';

class FlatProductCategoryList extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  rowView = ({item: row, index: i, highlightedCategory}) => {
    const {categoryId, category} = row;
 
    return <View key={categoryId} style={{width: '50%', paddingRight: 5, paddingLeft: 5, maxWidth: 250, maxHeight: 250}}>
      <Button style={{height: 'auto'}} large active={highlightedCategory && highlightedCategory.categoryId == row.categoryId} onPress={() => this.highlightCategory(row)}>
        <Icon name={row.categoryId}/>
      </Button>
      <Text style={styles.productSelectText}>{category}</Text>
    </View>;
  }

  headerView = ({selectedCategory}) => <Text note style={{marginBottom: 10}}>{selectedCategory.description}</Text>;

  highlightCategory = (highlightedCategory) =>  this.setState({highlightedCategory})

  navigateToCategory = () => {
    const {next, history, selectedCategory: parentSelectedCategory, highlightedCategory} = this.props;

    if (highlightedCategory.isLeaf) {
      history.push(next);
      this.setState({selectedCategory: highlightedCategory, parentSelectedCategory});
    } else {
      this.setState({selectedCategory: highlightedCategory});
    }
  }

  goBack = () => {
    const {history, rootProductCategory, parentSelectedCategory} = this.props;

    if (parentSelectedCategory == undefined || rootProductCategory.categoryId === parentSelectedCategory.categoryId){
      history.goBack();
    } else {
      this.navigateToCategory(parentSelectedCategory);
    }
  }

  render(){
    const {busy, history, defaultOptions, selectedCategory, highlightedCategory} = this.props;
    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>No items to display</Text>;

    return busy ? <LoadingScreen text="Loading Product Categories" /> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={this.goBack}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{this.resources.PageTitle}</Title></Body>
      </Header>
      <Content padded>
        <PagingListView
          style={styles.pagingListView}
          selectedCategory={selectedCategory}
          highlightedCategory={highlightedCategory}
          elementContainer={Row}
          elementContainerStyle={{flexWrap: 'wrap'}}
          daoName='productCategoryDao'
          dataPath={['product', 'categories']}
          pageSize={10}
          options={defaultOptions}
          history={history}
          rowView={this.rowView}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          headerView={this.headerView}
        />
      </Content>
      <ValidatingButton fullWidth paddedBottomLeftRight iconRight onPress={() => this.navigateToCategory()} validateOnMount={true} validationSchema={yup.object(validationSchema)} model={highlightedCategory}>
        <Text uppercase={false}>Continue</Text>
        <Icon next name='forward-arrow'/>
      </ValidatingButton>
    </Container>;
  }
}

FlatProductCategoryList.propTypes = {
  product: PropTypes.object,
  dispatch: PropTypes.func,
  screenProps: PropTypes.object,
  navigation: PropTypes.object
};

const validationSchema = {
  categoryId: yup.string().required(),
};

const styles = {
  productSelectText: {
    width: '100%',
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  },
  pagingListView: {
    backgroundColor: shotgun.brandPrimary,
    marginTop: 10
  }
};

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', 'Select Category').
    personell('Select Worker').
    rubbish('Commercial or domestic waste?');
/*eslint-enable */

const mapStateToProps = (state, initialProps) => {
  let {selectedContentType, selectedCategory, productCategory} = initialProps;
  const { rootProductCategory} = selectedContentType;
  selectedCategory = selectedCategory || productCategory;
  const defaultOptions = {
    ...getDaoOptions(state, 'productCategoryDao'),
    parentCategoryId: selectedCategory && selectedCategory.categoryId ? selectedCategory.categoryId : rootProductCategory
  };

  return {
    ...getNavigationProps(initialProps),
    ...initialProps,
    selectedCategory,
    rootProductCategory,
    selectedContentType,
    defaultOptions,
    categories: getDaoState(state, ['product', 'categories'], 'productCategoryDao'),
    busy: isAnyLoading(state, ['productDao', 'productCategoryDao']),
    errors: getLoadingErrors(state, ['productDao', 'productCategoryDao']),
  };
};

const ConnectedProductCategoryList =  withExternalState(mapStateToProps)(FlatProductCategoryList);
export default ConnectedProductCategoryList;

