import React from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, Text, TouchableHighlight} from 'react-native';
import {connect} from 'react-redux';
import {Spinner} from 'native-base';
import ProductCategoryDao from '../data/ProductCategoryDao';
import PagingListView from '../../common/components/PagingListView';

const ProductCategoryList = ({product, screenProps, navigation, dispatch}) => {
  const parentCategoryId = navigation.state.params !== undefined ? navigation.state.params.parentCategoryId : undefined;
  const productCategoryDao = new ProductCategoryDao(screenProps.client, dispatch, parentCategoryId);

  const styles = StyleSheet.create({
    container: {
      backgroundColor: '#FFFFFF',
      marginTop: 10
    },
    separator: {
      height: StyleSheet.hairlineWidth,
      backgroundColor: '#AAAAAA',
    }
  });

  const Paging = () => <View><Spinner /></View>;
  const NoItems = () => <View><Text>No items to display</Text></View>;
  const rowView = (row) => {
    const {categoryId, category} = row;

    //TODO - make this more flexible, might not be required depending on designs
    const navFunc =  () => {
      if (parentCategoryId == undefined) {
        navigation.navigate('ProductCategoryList', {parentCategoryId: categoryId, parentCategory: category});
      } else {
        navigation.navigate('ProductList', {categoryId, category});
      }
    };

    return <TouchableHighlight key={categoryId} style={{flex: 1, flexDirection: 'row'}} onPress={navFunc} underlayColor={'#EEEEEE'}>
    <View style={{flexDirection: 'column', flex: 1, padding: 0}}>
      <Text>{`${category}`}</Text>
    </View>
    </TouchableHighlight>;
  };

  return <PagingListView
    style={styles.container}
    dao={productCategoryDao}
    data={product.categories}
    pageSize={10}
    busy={product.status.busy}
    rowView={rowView}
    paginationWaitingView={Paging}
    emptyView={NoItems}
    headerView={() => null}
  />;
};

ProductCategoryList.propTypes = {
  product: PropTypes.object,
  dispatch: PropTypes.func,
  screenProps: PropTypes.object,
  navigation: PropTypes.object
};

ProductCategoryList.navigationOptions = ({navigation}) => {
  const title = navigation.state.params !== undefined ? navigation.state.params.parentCategory : undefined;
  const navOptions = {title};

  //hide the header if this is not a sub category
  if (title == undefined){
    navOptions.header = null;
  }
  return navOptions;
};

const mapStateToProps = ({ProductReducer}) => ({
  product: ProductReducer.product
});

export default connect(
  mapStateToProps
)(ProductCategoryList);

