import React, {Component} from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {View, Text, StyleSheet, TouchableHighlight} from 'react-native';
import {Spinner} from 'native-base';
import PagingListView from '../common/components/PagingListView';
import moment from 'moment';
import {Container, Content, Button} from 'native-base';
import {getDaoState, isAnyLoading} from 'common/dao';

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    display: 'flex',
    alignItems: 'flex-start',
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
});

class Orders extends Component{
  constructor(props){
    super(props);
    this.state = {isCompleted: false};
  }
    
  setIsCompleted(isCompleted){
    this.setState({isCompleted});
  }

  render(){
    const {isCompleted} = this.state;
    const {history, match} = this.props;

    const Paging = () => <View style={{flex: 1}}><Spinner /></View>;
    const NoItems = () => <View style={{flex: 1, display: 'flex'}}><Text>No orders to display</Text></View>;
    const rowView = (order) => {
      const created = moment(order.created);

      return <TouchableHighlight key={order.orderId} style={{flex: 1, flexDirection: 'row'}} onPress={() => history.push(`${match.path}/OrderDetail`, {orderId: order.orderId})} underlayColor={'#EEEEEE'}>
        <View style={{flexDirection: 'column', display: 'flex', flex: 1, padding: 0}}>
          <Text>{`Order: ${order.orderId}`}</Text>
          <Text>{`£${order.totalPrice} (${order.totalQuantity} items) ${order.status}`}</Text>
          <Text>{`${created.format('DD/MM/YYYY HH:mmZ')}`}</Text>
        </View>
      </TouchableHighlight>;
    };


    return <Container>
      <View style={{flexDirection: 'row', height: 25}}>
        <Button  onPress={() => this.setIsCompleted(false)}><Text>Pending</Text></Button>
        <Button onPress={() => this.setIsCompleted(true)}><Text>Complete</Text></Button>
      </View>
      {/*Render a big area underneath them for some reason<Tabs initialPage={0} onChangeTab={({ref}) => ref.props.onPress()}>
            <Tab heading="Pending" onPress={() => this.setIsCompleted(false)}/>
            <Tab heading="Complete" onPress={() => this.setIsCompleted(true)}/>
          </Tabs>*/}
      <Content>
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
      </Content>
    </Container>;
  }
}

Orders.PropTypes = {
  customer: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  orders: getDaoState(state, ['orders'], 'orderSummaryDao'),
  busy: isAnyLoading(state, ['orderSummaryDao', 'paymentDao']),
  ...initialProps
});

export default connect(
  mapStateToProps
)(Orders);
