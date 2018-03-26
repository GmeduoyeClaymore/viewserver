import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {View} from 'react-native';
import {Text, Spinner, Button, Container, Header, Title, Body, Left, Content, Row} from 'native-base';
import {LoadingScreen, PagingListView, ValidatingButton, Icon} from 'common/components';
import {connect} from 'custom-redux';
import yup from 'yup';
import * as ContentTypes from 'common/constants/ContentTypes';
import shotgun from 'native-base-theme/variables/shotgun';
import {isAnyLoading, getLoadingErrors, getDaoOptions, getNavigationProps, getDaoState} from 'common/dao';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', 'Select Category').
    personell('Select Worker').
    rubbish('Commercial or domestic waste?');
/*eslint-enable */

class FlatProductCategoryList extends Component{
  constructor(props){
    super(props);
    this.setCategory = this.setCategory.bind(this);
    this.rowView = this.rowView.bind(this);
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);

    this.state = {
      selectedCategory: props.selectedCategory || {},
      parentSelectedCategory: props.parentSelectedCategory || {}
    };
  }

  componentWillMount(){
    const {parentSelectedCategory, context} = this.props;
    context.setState({parentSelectedCategory});
  }

  componentWillReceiveProps(nextProps) {
    ContentTypes.resolveResourceFromProps(nextProps, resourceDictionary, this);
  }

  rowView({item: row, index: i, selectedCategory}){
    const {categoryId, category, imageUrl} = row;
 
    return <View key={categoryId} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
      <Button style={{height: 'auto'}} large active={selectedCategory.categoryId == row.categoryId} onPress={() => this.setCategory(row)}>
        <Icon name={imageUrl || 'dashed'}/>
      </Button>
      <Text style={styles.productSelectText}>{category}</Text>
    </View>;
  }

  headerView({selectedCategory}){ return (selectedCategory ? <Text note style={{marginBottom: 10}}>{selectedCategory !== undefined ? selectedCategory.description : null}</Text> : null);}

  navigateToCategory(){
    const {navigationStrategy, context} = this.props;
    const {selectedCategory} = this.state;

    if (selectedCategory.isLeaf) {
      context.setState({selectedCategory});
      navigationStrategy.next();
    } else {
      context.setState({parentSelectedCategory: selectedCategory});
      this.setState({parentSelectedCategory: selectedCategory, selectedCategory: undefined});
    }
  }

  setCategory(selectedCategory){
    this.setState({selectedCategory});
  }

  render(){
    const {busy, navigationStrategy, history, rootProductCategory, defaultOptions} = this.props;
    const {selectedCategory, parentSelectedCategory} = this.state;
    const Paging = () => <Spinner />;
    const NoItems = () => <Text empty>No items to display</Text>;

    return busy ? <LoadingScreen text="Loading Product Categories" /> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => rootProductCategory.categoryId === parentSelectedCategory.categoryId ?  navigationStrategy.prev() : this.navigateToCategory()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{this.resources.PageTitle}</Title></Body>
      </Header>
      <Content padded>
        <PagingListView
          style={styles.pagingListView}
          selectedCategory={selectedCategory}
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
      <ValidatingButton fullWidth paddedBottom iconRight onPress={() => this.navigateToCategory()} validateOnMount={true} validationSchema={yup.object(validationSchema)} model={selectedCategory}>
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

const mapStateToProps = (state, initialProps) => {
  const {context} = initialProps;
  const {selectedContentType, selectedCategory} = context.state;
  const {productCategory: rootProductCategory} = selectedContentType;

  const defaultOptions = {
    ...getDaoOptions(state, 'productCategoryDao'),
    parentCategoryId: selectedCategory && selectedCategory.categoryId ? selectedCategory.categoryId : rootProductCategory.categoryId
  };

  return {
    ...getNavigationProps(initialProps),
    rootProductCategory,
    selectedContentType,
    defaultOptions,
    categories: getDaoState(state, ['product', 'categories'], 'productCategoryDao'),
    busy: isAnyLoading(state, ['productDao', 'productCategoryDao']),
    errors: getLoadingErrors(state, ['productDao', 'productCategoryDao']),
    ...initialProps
  };
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

const ConnectedProductCategoryList =  connect(mapStateToProps)(FlatProductCategoryList);
export default ConnectedProductCategoryList;
