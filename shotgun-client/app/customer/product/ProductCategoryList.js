import React from 'react';
import PropTypes from 'prop-types';
import {View, StyleSheet, Text, TouchableHighlight} from 'react-native';
import {Spinner} from 'native-base';
import PagingListView from 'common/components/PagingListView';

const ProductCategoryList = ({screenProps, navigation}) => {
  const parentCategoryId = navigation.state.params !== undefined ? navigation.state.params.parentCategoryId : 'NONE';
  const options = {parentCategoryId};

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
    daoName='productCategory'
    dataPath={['product', 'categories']}
    pageSize={10}
    options={options}
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

export default ProductCategoryList;

