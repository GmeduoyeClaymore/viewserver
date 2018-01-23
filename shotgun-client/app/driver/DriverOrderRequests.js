import React from 'react';
import {connect} from 'react-redux';
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

const DriverOrderRequests = ({history, isDelivery, vehicle = {}, position, busy}) => {
  if (busy){
    return <LoadingScreen text="Loading Map" />;
  }


  const {location} = history;
  const {vehicleTypeId, noRequiredForOffload = 0} = vehicle;
  const productId = isDelivery ? Products.DELIVERY : Products.DISPOSAL;
  const maxDistance = 30; //max distance to show jobs in miles

  const reportOptions = {
    productId,
    vehicleTypeId,
    noRequiredForOffload,
    maxDistance,
    driverLatitude: position.latitude,
    driverLongitude: position.longitude
  };

  const Paging = () => <View style={{flex: 1}}><Spinner /></View>;
  const NoItems = () => <View style={{flex: 1, display: 'flex'}}><Text>No jobs available</Text></View>;
  const RowView = (orderSummary) => <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} next='/Driver/DriverOrderRequestDetail'/>;

  const onChangeTab = (newIsDelivery) => {
    if (isDelivery !== newIsDelivery) {
      history.replace(location.pathname, {isDelivery: newIsDelivery});
    }
  };

  return <Container>
    <Header hasTabs>
      <Body><Title>Available Jobs</Title></Body>
    </Header>
    <Tabs initialPage={isDelivery ? 0 : 1} {...shotgun.tabsStyle} onChangeTab={({i}) => onChangeTab(i == 0)}>
      <Tab heading="Deliveries" />
      <Tab heading="Waste Collection" />
    </Tabs>
    <Content>
      <List>
        <PagingListView
          daoName='orderRequestDao'
          dataPath={['driver', 'orders']}
          rowView={RowView}
          options={reportOptions}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          pageSize={10}
          headerView={() => null}
        />
      </List>
    </Content>
  </Container>;
};

const mapStateToProps = (state, initialProps) => {
  const vehicle = getDaoState(state, ['vehicle'], 'vehicleDao');
  const position = getDaoState(state, ['position'], 'userDao');

  return {
    ...initialProps,
    isDelivery: initialProps.history.location.state && initialProps.history.location.state.isDelivery !== undefined ? initialProps.history.location.state.isDelivery : true,
    busy: isAnyLoading(state, ['vehicleDao', 'userDao']) || !vehicle || !position,
    vehicle,
    position
  };
};

export default withRouter(connect(
  mapStateToProps
)(DriverOrderRequests));
