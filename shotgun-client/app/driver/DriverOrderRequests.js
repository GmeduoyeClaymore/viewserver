import React, {Component} from 'react';
import {connect, ReduxRouter, Route, memoize} from 'custom-redux';
import { PagingListView, LoadingScreen, OrderRequest, Tabs } from 'common/components';
import { View, Text, Container, Spinner, Header, Body, Title, Tab } from 'native-base';
import { getDaoState, isAnyLoading, getNavigationProps, resetSubscriptionAction} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';

const DRIVER_ORDER_REQUEST_DEFAULT_OPTIONS = {
  columnsToSort: [{ name: 'from', direction: 'asc' }, { name: 'orderId', direction: 'asc' }],
  reportId: 'driverOrderRequest'
};

const Paging = () => <Spinner />;
const NoItems = () => <Text empty>No jobs available</Text>;
const RowView = ({ item: orderSummary, isLast, isFirst, history, parentPath }) => <OrderRequest history={history} orderSummary={orderSummary} key={orderSummary.orderId} isLast={isLast} isFirst={isFirst} next={`${parentPath}/DriverOrderRequestDetail`} />;

const getDefaultOptions = (contentType, contentTypeOptions) => {
  return {
    ...DRIVER_ORDER_REQUEST_DEFAULT_OPTIONS,
    userId: undefined,
    contentType,
    contentTypeOptions
  };
};
const OrderView = ({history, parentPath, contentType, contentTypeOptions, height, width}) => (
  <View style={{ flex: 1, height, width}}>
    <PagingListView
      ref={c=> {this.pagingListView = c;}}
      daoName='orderRequestDao'
      parentPath={parentPath}
      history={history}
      dataPath={['driver', 'orders']}
      rowView={RowView}
      options={getDefaultOptions(contentType, contentTypeOptions)}
      paginationWaitingView={Paging}
      emptyView={NoItems}
      pageSize={10}
      headerView={() => null}
    />
  </View>
);
class DriverOrderRequests extends Component{
  constructor(props){
    super(props);
    this.onChangeTab = this.onChangeTab.bind(this);
    this.state  = {};
  }
  
  onChangeTab(index){
    const {selectedContentTypes, history, path, isInBackground} = this.props;
    if (isInBackground){
      return;
    }
    const newSelectedContentType = selectedContentTypes[index];
    history.replace(`${path}/ContentTypeId${newSelectedContentType.contentTypeId}X`);
  }
  

  render(){
    const { busy, selectedContentTypes = [], height, history, navContainerOverride, parentPath, path, contentTypeOptions, selectedContentTypeIndex} = this.props;
    if (busy) {
      return <LoadingScreen text="Loading jobs..." />;
    }
  
    const {onChangeTab} = this;
    return <View style={{flex: 1}}>
      <Header hasTabs>
        <Body><Title>Available Jobs</Title></Body>
      </Header>
      <Tabs initialPage={selectedContentTypeIndex} page={selectedContentTypeIndex} {...shotgun.tabsStyle} onChangeTab={({ i }) => onChangeTab(i)}>
        {selectedContentTypes.map(c => <Tab key={c.name} heading={c.name} />)}
      </Tabs>
      {selectedContentTypes[0] ? <ReduxRouter  name="DriverOrderRequestRouter" height={height - 150} defaultRoute={`ContentTypeId${selectedContentTypes[0].contentTypeId}X`} {...{busy, selectedContentTypes, navContainerOverride, history, path, parentPath, contentTypeOptions}}>
        {selectedContentTypes.map(c => <Route key={c.contentTypeId} parentPath={parentPath} path={`ContentTypeId${c.contentTypeId}X`} contentType={c} component={OrderView} />)}
      </ReduxRouter> : null}
    </View>;
  }
}


const getSelectedContentTypeFromLocation = memoize((history, selectedContentTypes) => {
  const {location} = history;
  if (!selectedContentTypes){
    return 0;
  }
  if (!location.pathname.includes('/ContentTypeId')){
    return 0;
  }
  return selectedContentTypes.find(element => { return location.pathname.includes(`/ContentTypeId${element.contentTypeId}X`);});
});

const getSelectedContentTypesFromUser = memoize((user, availableContentTypes) => {
  const selectedContentTypeOptions = JSON.parse(user.selectedContentTypes);
  const selectedContentTypeIds = selectedContentTypeOptions ? Object.keys(selectedContentTypeOptions).filter(c => !!selectedContentTypeOptions[c]).map( key => parseInt(key, 10)) : [];
  return {
    selectedContentTypes: availableContentTypes.filter(ct => !!~selectedContentTypeIds.indexOf(ct.contentTypeId)),
    selectedContentTypeOptions
  };
});

const mapStateToProps = (state, initialProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  if (!user){
    return;
  }
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao') || [];
  const navigationProps = getNavigationProps(initialProps) || [];
  const {history} = initialProps;
  const {selectedContentTypes, selectedContentTypeOptions} = getSelectedContentTypesFromUser(user, contentTypes);
  const selectedContentType = getSelectedContentTypeFromLocation(history, selectedContentTypes) || selectedContentTypes[0] || {};
  const {contentTypeId} = selectedContentType;
  const contentTypeOptions = contentTypeId ? selectedContentTypeOptions[contentTypeId] : {};
  let selectedContentTypeIndex = selectedContentTypes.indexOf(selectedContentType);
  selectedContentTypeIndex = !!~selectedContentTypeIndex ? selectedContentTypeIndex : 0;
  return {
    ...initialProps,
    selectedContentTypes,
    contentTypeOptions,
    selectedContentTypeIndex,
    contentTypeId,
    selectedContentType,
    isDelivery: navigationProps.isDelivery !== undefined ? navigationProps.isDelivery : true,
    busy: isAnyLoading(state, ['vehicleDao', 'userDao']) || !selectedContentType,
    user
  };
};

export default connect(
  mapStateToProps
)(DriverOrderRequests);
