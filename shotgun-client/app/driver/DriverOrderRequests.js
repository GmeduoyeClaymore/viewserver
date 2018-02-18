import React from 'react';
import { connect } from 'custom-redux';
import { PagingListView, LoadingScreen, OrderRequest, Tabs } from 'common/components';
import { withRouter } from 'react-router';
import { View, Text, Container, Spinner, Header, Body, Title, Tab } from 'native-base';
import { getDaoState, isAnyLoading, getNavigationProps } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';

const DriverOrderRequests = ({ history, selectedContentTypeIndex, position, busy, selectedContentTypes, contentTypeOptions, selectedContentType = {}, contentTypeId }) => {
  if (busy) {
    return <LoadingScreen text="Loading Map" />;
  }

  const Paging = () => <Spinner />;
  const NoItems = () => <Text empty>No jobs available</Text>;
  const RowView = ({ item: orderSummary, isLast, isFirst }) => <OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} isLast={isLast} isFirst={isFirst} next='/Driver/DriverOrderRequestDetail' />;

  const onChangeTab = (index) => {
    const newSelectedContentType = selectedContentTypes[index];
    if (selectedContentType.contentTypeId !== newSelectedContentType.contentTypeId) {
      history.replace(location.pathname, { contentTypeId: newSelectedContentType.contentTypeId });
    }
  };

  return <Container>
    <Header hasTabs>
      <Body><Title>xAvailable Jobs {JSON.stringify(position)}</Title></Body>
    </Header>
    <Tabs initialPage={selectedContentTypeIndex} {...shotgun.tabsStyle} onChangeTab={({ i }) => onChangeTab(i)}>
      {selectedContentTypes.map(c => <Tab key={c.name} heading={c.name} />)}
    </Tabs>
    <View style={{ flex: 1 }}>
      <PagingListView
        daoName='orderRequestDao'
        dataPath={['driver', 'orders']}
        rowView={RowView}
        options={{contentTypeId, contentTypeOptions, position}}
        paginationWaitingView={Paging}
        emptyView={NoItems}
        pageSize={10}
        headerView={() => null}
      />
    </View>
  </Container>;
};

const mapStateToProps = (state, initialProps) => {
  const vehicle = getDaoState(state, ['vehicle'], 'vehicleDao');
  const position = getDaoState(state, ['position'], 'userDao');
  const user = getDaoState(state, ['user'], 'userDao');
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao');
  const navigationProps = getNavigationProps(initialProps) || [];
  let { contentTypeId } = navigationProps;
  contentTypeId = contentTypeId || (contentTypes[0] && contentTypes[0].contentTypeId);
  const selectedContentTypeOptions = JSON.parse(user.selectedContentTypes);
  const selectedContentTypeIds = selectedContentTypeOptions ? Object.keys(selectedContentTypeOptions).filter(c => !!selectedContentTypeOptions[c]) : [];
  const selectedContentTypes = contentTypes.filter(ct => !!~selectedContentTypeIds.indexOf(ct.contentTypeId));
  const selectedContentType = contentTypes.find(ct => ct.contentTypeId === contentTypeId) || contentTypes[0];
  const selectedContentTypeIndex = selectedContentTypes.indexOf(selectedContentType);
  const contentTypeOptions = contentTypeId ? selectedContentTypeOptions[contentTypeId] : {};

  return {
    ...initialProps,
    selectedContentTypes,
    contentTypeOptions,
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
