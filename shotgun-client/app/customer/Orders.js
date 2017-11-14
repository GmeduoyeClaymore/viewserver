import React from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {View, Text, StyleSheet, TouchableHighlight} from 'react-native';
import {Spinner} from 'native-base';
import PagingListView from '../common/components/PagingListView';
import OrderSummaryDao from './data/OrderSummaryDao';
import { TabNavigator } from 'react-navigation';
import moment from 'moment';

const Orders = ({customer, isCompleted, dispatch, screenProps, navigation}) => {
    const {customerId} = screenProps.customerService;
    const orderSummaryDao = new OrderSummaryDao(screenProps.client, dispatch, customerId, isCompleted);
    const orders = isCompleted ? customer.orders.complete : customer.orders.incomplete;

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
    const NoItems = () => <View><Text>No orders to display</Text></View>;
    const rowView = (order) => {
      const created = moment(order.created);
      const orderComplete = order.status == 'COMPLETED';

      return <TouchableHighlight key={order.orderId} style={{flex: 1, flexDirection: 'row'}} onPress={() => navigation.navigate('OrderDetail', {orderId: order.orderId, isCompleted: orderComplete})} underlayColor={'#EEEEEE'}>
        <View style={{flexDirection: 'column', flex: 1, padding: 0}}>
          <Text>{`Order: ${order.orderId}`}</Text>
          <Text>{`£${order.totalPrice} (${order.totalQuantity} items) ${order.status}`}</Text>
          <Text>{`${created.format('DD/MM/YYYY HH:mmZ')}`}</Text>
        </View>
      </TouchableHighlight>;
    };

    return <PagingListView
      style={styles.container}
      dao={orderSummaryDao}
      data={orders}
      pageSize={10}
      busy={customer.status.busy}
      rowView={rowView}
      paginationWaitingView={Paging}
      emptyView={NoItems}
      headerView={() => null}
    />;
};

Orders.PropTypes = {
  customer: PropTypes.object
};

Orders.navigationOptions = {header: null};

const mapStateToProps = ({CustomerReducer}) => ({
  customer: CustomerReducer.customer
});

const ConnectedOrders = connect(
  mapStateToProps
)(Orders);

const tabNavigator = TabNavigator({
  Pending: { screen: props => <ConnectedOrders {...props} isCompleted={false}/>},
  Completed: { screen: props => <ConnectedOrders {...props} isCompleted={true}/> }
});

tabNavigator.navigationOptions = {header: null};

export default tabNavigator;


