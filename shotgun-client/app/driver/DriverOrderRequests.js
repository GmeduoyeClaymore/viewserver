import React, {Component} from 'react';
import {connect, ReduxRouter, Route} from 'custom-redux';
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
    contentType,
    ...contentTypeOptions
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
    const {selectedContentTypes, history, path} = this.props;
    const newSelectedContentType = selectedContentTypes[index];
    history.replace(`${path}/ContentTypeId${newSelectedContentType.contentTypeId}X`);
  }
  

  render(){
    const { busy, selectedContentTypes, height, history, parentPath, path, contentTypeOptions, selectedContentTypeIndex} = this.props;
    if (busy) {
      return <LoadingScreen text="Loading jobs..." />;
    }
  
    const {onChangeTab} = this;
    return <Container>
      <Header hasTabs>
        <Body><Title>Available Jobs</Title></Body>
      </Header>
      <Tabs initialPage={selectedContentTypeIndex} {...shotgun.tabsStyle} onChangeTab={({ i }) => onChangeTab(i)}>
        {selectedContentTypes.map(c => <Tab key={c.name} heading={c.name} />)}
      </Tabs>
      {selectedContentTypes[0] ? <ReduxRouter  name="DriverOrderRequestRouter" height={height - 150} defaultRoute={`${path}/ContentTypeId${selectedContentTypes[0].contentTypeId}X`} {...{busy, selectedContentTypes, history, parentPath, contentTypeOptions}}>
        {selectedContentTypes.map(c => <Route key={c.contentTypeId} parentPath={parentPath} path={`${path}/ContentTypeId${c.contentTypeId}X`} contentType={c} component={OrderView} />)}
      </ReduxRouter> : null}
    </Container>;
  }
}


const getSelectedContentTypeFromLocation = (history, selectedContentTypes) => {
  const {location} = history;
  if (!selectedContentTypes){
    return 0;
  }
  if (!location.pathname.includes('/ContentTypeId')){
    return 0;
  }
  return selectedContentTypes.find(element => { location.pathname.includes(`/ContentTypeId${element.contentTypeId}X`);});
};

const mapStateToProps = (state, initialProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao') || [];
  const navigationProps = getNavigationProps(initialProps) || [];
  const {history} = initialProps;
  const selectedContentTypeOptions = JSON.parse(user.selectedContentTypes);
  const selectedContentTypeIds = selectedContentTypeOptions ? Object.keys(selectedContentTypeOptions).filter(c => !!selectedContentTypeOptions[c]).map( key => parseInt(key, 10)) : [];
  const selectedContentTypes = contentTypes.filter(ct => !!~selectedContentTypeIds.indexOf(ct.contentTypeId));
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
