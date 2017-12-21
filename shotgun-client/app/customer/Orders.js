import React, {Component} from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {View, Text, StyleSheet} from 'react-native';
import PagingListView from '../common/components/PagingListView';
import { withRouter } from 'react-router';
import {Container, Content, Button, Spinner, Header, Body, Title, Item, Icon, Grid, Row, Col, Tabs, Tab, List, ListItem} from 'native-base';
import {getDaoState, isAnyLoading} from 'common/dao';
import shotgun from '../native-base-theme/variables/shotgun';
import {getFriendlyOrderStatusName, OrderStatuses} from 'common/constants/OrderStatuses';

class Orders extends Component{
  constructor(props){
    super(props);
    this.state = {isCompleted: true};
  }
    
  setIsCompleted(isCompleted){
    if (this.state.isCompleted !== isCompleted) {
      this.setState({isCompleted});
    }
  }

  render(){
    const {isCompleted} = this.state;
    const {history} = this.props;

    const Paging = () => <View style={{flex: 1}}><Spinner /></View>;
    const NoItems = () => <View style={{flex: 1, display: 'flex'}}><Text>No orders to display</Text></View>;

    const CtaView = ({orderSummary}) => {
      const isComplete = orderSummary.status == OrderStatuses.COMPLETED;
      const isInProgress = orderSummary.status == OrderStatuses.PICKEDUP;
      const isDelivery = orderSummary.productId == 'PROD_Delivery';

      if (isComplete){
        return null;
      }

      if (isInProgress){
        return <Col style={styles.ctaContainer}>
          <Button style={styles.trackButton} onPress={() => console.log('Should go to track driver screen')}>
            <Text style={styles.primaryText}>Track Driver</Text>
            <Icon style={styles.primaryText} name='arrow-forward' />
          </Button>
        </Col>;
      }
      return <Col style={styles.ctaContainer}>
        <Button style={styles.cancelButton} onPress={() => console.log('Should cancel order')}>
          <Text style={styles.dangerText}>Cancel {isDelivery ? 'delivery' : 'collection'}</Text>
        </Button>
      </Col>;
    };

    const rowView = (orderSummary) => {
      const {orderItem, delivery, orderId, status} = orderSummary
      const {origin, destination} = delivery;

      const isDelivery = orderItem.productId == 'PROD_Delivery';

      return <ListItem key={orderId}>
        <Grid>
          <Row size={50} onPress={() => history.push('/Customer/OrderDetail', {orderSummary})}>
            <Col size={10}><Icon name={isDelivery ? 'car' : 'trash'} /></Col>
            <Col size={70}>
              <Row><Text>Order: #1234 eta: {orderItem.productId}</Text></Row>
              <Row>
                <Text>{origin.line1}, {origin.postCode}</Text>
                {isDelivery ? <Text>{destination.line1}, {destination.postCode}</Text> : null}
              </Row>
            </Col>
            <Col size={20}><Text>Details ></Text></Col>
          </Row>
          <Row size={50}>
            <Col style={styles.statusContainer}><Text style={styles.status}>{getFriendlyOrderStatusName(status)}</Text></Col>
            <CtaView orderSummary={orderSummary}/>
          </Row>
        </Grid>
      </ListItem>;
    };

    return <Container>
      <Header hasTabs>
        <Body><Title>My Jobs</Title></Body>
      </Header>
      <Tabs initialPage={isCompleted ? 1 : 0} style={{flex: 0}} {...styles.tabsStyle} onChangeTab={({i}) => this.setIsCompleted(i == 1)}>
        <Tab {...styles.tabStyle} heading="Live Jobs"/>
        <Tab {...styles.tabStyle} heading="Complete"/>
      </Tabs>
      <Content>
        <List>
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
        </List>
      </Content>
    </Container>;
  }
}

const styles = {
  container: {
    backgroundColor: '#FFFFFF',
    display: 'flex',
    alignItems: 'flex-start',
  },
  orderItemRow: {
    height: 90
  },
  tabsStyle: {
    tabBarUnderlineStyle: {
      backgroundColor: 'blue',
    }
  },
  tabStyle: {
    tabStyle: {
      backgroundColor: shotgun.brandPrimary
    },
    activeTabStyle: {
      backgroundColor: shotgun.brandPrimary
    },
    textStyle: {
      color: shotgun.textColor
    },
    activeTextStyle: {
      color: shotgun.textColor
    }
  },
  primaryText: {
    color: shotgun.textColor
  },
  dangerText: {
    color: shotgun.brandDanger
  },
  status: {
    color: shotgun.inverseTextColor
  },
  statusContainer: {
    backgroundColor: shotgun.brandSecondary,
    justifyContent: 'center',
    alignItems: 'center',
  },
  ctaContainer: {
    borderTopWidth: 1,
    borderColor: '#f4f4f4',
    backgroundColor: shotgun.brandPrimary,
    justifyContent: 'center',
    alignItems: 'center',
  },
  trackButton: {
    margin: 0,
    borderRadius: 0,
    flex: 1,
    backgroundColor: shotgun.brandPrimary,
    alignSelf: 'stretch',
    justifyContent: 'center',
    alignItems: 'center',
  },
  cancelButton: {
    margin: 0,
    borderRadius: 0,
    flex: 1,
    backgroundColor: shotgun.brandPrimary,
    alignSelf: 'stretch',
    justifyContent: 'center',
    alignItems: 'center',
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
};

Orders.PropTypes = {
  customer: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  orders: getDaoState(state, ['orders'], 'orderSummaryDao'),
  busy: isAnyLoading(state, ['orderSummaryDao', 'paymentDao']),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(Orders));
