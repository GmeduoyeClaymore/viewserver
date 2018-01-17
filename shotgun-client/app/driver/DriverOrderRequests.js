import React, {Component} from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {View, Text} from 'react-native';
import PagingListView from 'common/components/PagingListView';
import { withRouter } from 'react-router';
import {Container, Content, Spinner, Header, Body, Title, Tab, List} from 'native-base';
import LoadingScreen from 'common/components/LoadingScreen';
import {getDaoState, isAnyLoading} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import Products from 'common/constants/Products';
import OrderRequest from 'common/components/OrderRequest';
import Tabs from 'common/components/Tabs';

class DriverOrderRequests extends Component{
  constructor(props){
    super(props);
    this.state = {isDelivery: true};
  }

  setIsDelivery(isDelivery){
    if (this.state.isDelivery !== isDelivery) {
      this.setState({isDelivery});
    }
  }

  render(){
    const {isDelivery} = this.state;
    const {vehicle = {}, position, busy} = this.props;
    const {vehicleTypeId, noRequiredForOffload = 0} = vehicle;
    const productId = isDelivery ? Products.DELIVERY : Products.DISPOSAL;
    const maxDistance = 30; //max distance to show jobs in miles

    const Paging = () => <View style={{flex: 1}}><Spinner /></View>;
    const NoItems = () => <View style={{flex: 1, display: 'flex'}}><Text>No jobs available</Text></View>;
    const RowView = (orderSummary) => <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} next='/Driver/DriverOrderRequestDetail'/>;

    return busy ? <LoadingScreen text="Loading Map" /> : <Container>
      <Header hasTabs>
        <Body><Title>Available Jobs</Title></Body>
      </Header>
      <Tabs initialPage={isDelivery ? 0 : 1} {...shotgun.tabsStyle} onChangeTab={({i}) => this.setIsDelivery(i == 0)}>
        <Tab heading="Deliveries" />
        <Tab heading="Waste Collection" />
      </Tabs>
      <Content>
        <List>
          <PagingListView
            daoName='orderRequestDao'
            dataPath={['driver', 'orders']}
            rowView={RowView}
            options={{productId, vehicleTypeId, noRequiredForOffload, maxDistance, driverLatitude: position.latitude, driverLongitude: position.longitude}}
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

DriverOrderRequests.PropTypes = {
  customer: PropTypes.object
};

const mapStateToProps = (state, initialProps) => {
  const vehicle = getDaoState(state, ['vehicle'], 'vehicleDao');
  const position = getDaoState(state, ['position'], 'userDao');
    return {
      busy: isAnyLoading(state, ['vehicleDao', 'userDao']) || !vehicle || !position,
      vehicle,
      position,
      ...initialProps
    };
  };

export default withRouter(connect(
  mapStateToProps
)(DriverOrderRequests));
