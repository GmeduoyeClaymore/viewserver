import React from 'react';
import {connect} from 'react-redux';
import {View, Text} from 'react-native';
import PagingListView from 'common/components/PagingListView';
import { withRouter } from 'react-router';
import {Container, Content, Spinner, Header, Body, Title, Tab, List} from 'native-base';
import LoadingScreen from 'common/components/LoadingScreen';
import {getDaoState, isAnyLoading, getNavigationProps} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import OrderRequest from 'common/components/OrderRequest';
import Tabs from 'common/components/Tabs';

const DriverOrderRequests = ({history, selectedContentTypeIndex, vehicle = {}, position, busy, selectedContentTypes, selectedContentType = {}, contentTypeId}) => {
  if (busy){
    return <LoadingScreen text="Loading Map" />;
  }


  const {location} = history;
  const {vehicleTypeId, noRequiredForOffload = 0} = vehicle;
  const maxDistance = 30; //max distance to show jobs in miles

  const reportOptions = {
    contentTypeId,
    vehicleTypeId,
    noRequiredForOffload,
    maxDistance,
    driverLatitude: position.latitude,
    driverLongitude: position.longitude
  };

  const Paging = () => <View style={{flex: 1}}><Spinner /></View>;
  const NoItems = () => <View style={{flex: 1, display: 'flex'}}><Text>No jobs available</Text></View>;
  const RowView = ({item: orderSummary, isLast, isFirst}) => <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} isLast={isLast} isFirst={isFirst} next='/Driver/DriverOrderRequestDetail'/>;

  const onChangeTab = (index) => {
    const newSelectedContentType = selectedContentTypes[index];
    if (selectedContentType.contentTypeId !== newSelectedContentType.contentTypeId){
      history.replace(location.pathname, {contentTypeId: newSelectedContentType.contentTypeId});
    }
  };

  return <Container>
    <Header hasTabs>
      <Body><Title>Available Jobs</Title></Body>
    </Header>
    <Tabs initialPage={selectedContentTypeIndex} {...shotgun.tabsStyle} onChangeTab={({i}) => onChangeTab(i)}>
      {selectedContentTypes.map(c => <Tab key={c.name} heading={c.name} />)}
    </Tabs>
    <Content>
      <List style={{backgroundColor: shotgun.hairline}}>
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
  const user = getDaoState(state, ['user'], 'userDao');
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao');
  const navigationProps = getNavigationProps(initialProps) || [];
  let {contentTypeId} = navigationProps;
  contentTypeId = contentTypeId || (contentTypes[0] && contentTypes[0].contentTypeId);
  const selectedContentTypeIds = user ? user.selectedContentTypes.split(',').map( str => parseInt(str, 10)) : [];
  const selectedContentTypes = contentTypes.filter(ct => !!~selectedContentTypeIds.indexOf(ct.contentTypeId));
  const selectedContentType = contentTypes.find(ct => ct.contentTypeId === contentTypeId) || contentTypes[0];
  const selectedContentTypeIndex = selectedContentTypes.indexOf(selectedContentType);

  return {
    ...initialProps,
    selectedContentTypes,
    contentTypeId,
    selectedContentType,
    selectedContentTypeIndex,
    isDelivery: navigationProps.isDelivery !== undefined ? navigationProps.isDelivery : true,
    busy: isAnyLoading(state, ['vehicleDao', 'userDao']) || !vehicle || !position,
    vehicle,
    user,
    position
  };
};

export default withRouter(connect(
  mapStateToProps
)(DriverOrderRequests));
