import React, {Component} from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {View, Text, StyleSheet, TouchableHighlight} from 'react-native';
import {Spinner} from 'native-base';
import PagingListView from '../common/components/PagingListView';
import { TabNavigator } from 'react-navigation';
import moment from 'moment';
import { Button, Icon} from 'native-base';
import {getDaoState, isAnyLoading} from 'common/dao';

const Paging = () => <View style={{flex : 1}}><Spinner /></View>;
const NoItems = () => <View style={{flex : 1, display : 'flex'}}><Text>No orders to display</Text></View>;
const rowView = (order) => {
  const created = moment(order.created);
  const orderComplete = order.status == 'COMPLETED';

  return <TouchableHighlight key={order.orderId} style={{flex: 1, flexDirection: 'row'}} onPress={() => navigation.navigate('OrderDetail', {orderId: order.orderId, isCompleted: orderComplete})} underlayColor={'#EEEEEE'}>
    <View style={{flexDirection: 'column', display:'flex', flex: 1, padding: 0}}>
      <Text>{`Order: ${order.orderId}`}</Text>
      <Text>{`£${order.totalPrice} (${order.totalQuantity} items) ${order.status}`}</Text>
      <Text>{`${created.format('DD/MM/YYYY HH:mmZ')}`}</Text>
    </View>
  </TouchableHighlight>;
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    display: 'flex'
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
});


class Orders extends Component{

    constructor(props){
      super(props);
      this.state = {isCompleted : true}
    }
    
    setIsCompleted(isCompleted){
      this.setState({isCompleted});
    }

    render(){ 
      const {isCompleted} = this.state;
      return <View style={{flex: 1, display : 'flex', flexDirection: 'column'}}>
      <View style={{flexDirection : 'row', height : 25}}>
        <Button style={{flex: 1, margin: 3, backgroundColor : 'blue'}} transparent dark onPress={() => this.setIsCompleted(false)}><Text>Pending</Text></Button>
        <Button style={{flex: 1, margin: 3, backgroundColor : 'blue' }} transparent dark onPress={() => this.setIsCompleted(true)}><Text>Complete</Text></Button>
      </View>      
      <View style={{height: 25, marginTop: 40}}><Text>{isCompleted ? "Completed Orders" : "Pending Orders"}</Text></View>
      <PagingListView
        daoName='orderSummaryDao'
        dataPath={['customer', 'orders']}
        style={styles.container}
        rowView={rowView}
        options={{isCompleted}}
        paginationWaitingView={Paging}
        emptyView={NoItems}
        pageSize={10}
        headerView={() => null}
      />
      </View>
    }
};

Orders.PropTypes = {
  customer: PropTypes.object
};

Orders.navigationOptions = {header: null};

const mapStateToProps = (state, initialProps) => ({
  orders: getDaoState(state, ['orders'], 'orderSummaryDao'),
  busy: isAnyLoading(state, ['orderSummaryDao', 'paymentCardsDao']),
  ...initialProps
});

export default connect(
  mapStateToProps
)(Orders);