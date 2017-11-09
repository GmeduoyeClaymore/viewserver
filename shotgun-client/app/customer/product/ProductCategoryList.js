import React, {PropTypes} from 'react';
import {View, StyleSheet, Text, TouchableHighlight} from 'react-native';
import ListViewDataSink from '../../common/dataSinks/ListViewDataSink';
import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';


const ProductCategoryList = ({screenProps, navigation}) => {
  const {client} = screenProps;
  const parentCategoryId = navigation.state.params !== undefined ? navigation.state.params.parentCategoryId : undefined;

  const reportContext = {
    reportId: 'productCategory',
    parameters: {
      parentCategoryId
    }
  };

  const options = {
    columnsToSort: [{name: 'category', direction: 'asc'}]
  };

  const subscriptionStrategy = new ReportSubscriptionStrategy(client, reportContext);

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

  const Paging = () => <View><Text>Paging...</Text></View>;
  const NoItems = () => <View><Text>No items to display</Text></View>;
  const LoadedAllItems = () => <View><Text>No More to display</Text></View>;
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


  return <ListViewDataSink
    ref={null}
    style={styles.container}
    subscriptionStrategy={subscriptionStrategy}
    options={options}
    rowView={rowView}
    paginationWaitingView={Paging}
    emptyView={NoItems}
    paginationAllLoadedView={LoadedAllItems}
    refreshable={true}
    enableEmptySections={true}
    renderSeparator={(sectionId, rowId) => <View key={rowId} style={styles.separator} />}
    headerView={() => null}
  />;
};

ProductCategoryList.propTypes = {
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


