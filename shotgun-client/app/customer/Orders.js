import React, {PropTypes} from 'react';
import {View, Text, StyleSheet} from 'react-native';
import ListViewDataSink from '../common/dataSinks/ListViewDataSink';
import ReportSubscriptionStrategy from '../common/subscriptionStrategies/ReportSubscriptionStrategy';
import { TabNavigator } from 'react-navigation';

const Orders = ({screenProps, isCompleted}) => {
  const {client} = screenProps;
  const {customerId} = screenProps.customerService;
  const reportContext = {
    reportId: 'orderSummary',
    parameters: {
      customerId,
      isCompleted
    }
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
  const NoItems = () => <View><Text>No orders to display</Text></View>;
  const LoadedAllItems = () => <View><Text>No More Orders to display</Text></View>;
  const rowView = (order) => {
    return <View key={order.orderId} style={{flexDirection: 'column', flex: 1, padding: 0}}>
      <Text>{`Order: ${order.orderId}`}</Text>
      <Text>{`£${order.totalPrice} (${order.totalQuantity} items) ${order.status}`}</Text>
      <Text>{`${order.createdDate}`}</Text>
    </View>;
  };

  return <ListViewDataSink
    ref={null}
    style={styles.container}
    subscriptionStrategy={subscriptionStrategy}
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

Orders.propTypes = {
  screenProps: PropTypes.object
};

export default TabNavigator({
  Pending: { screen: props => <Orders {...props} isCompleted={false}/>},
  Completed: { screen: props => <Orders {...props} isCompleted={true}/> }
},
{
  lazy: true
});


