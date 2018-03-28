import React, {Component} from 'react';
import { connect } from 'custom-redux';
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
const RowView = ({ item: orderSummary, isLast, isFirst, history }) => <OrderRequest history={history} orderSummary={orderSummary} key={orderSummary.orderId} isLast={isLast} isFirst={isFirst} next='/Driver/DriverOrderRequestDetail' />;

class DriverOrderRequests extends Component{
  constructor(props){
    super(props);
    this.onChangeTab = this.onChangeTab.bind(this);
    this.state  = {};
  }

  componentWillMount(){
    const {resetOrderRequests} = this.props;
    if (resetOrderRequests){
      resetOrderRequests();
    }
  }
  
  onChangeTab(index){
    const {selectedContentType, selectedContentTypes, history} = this.props;
    const newSelectedContentType = selectedContentTypes[index];
    if (selectedContentType.contentTypeId !== newSelectedContentType.contentTypeId) {
      history.replace('/Driver/DriverOrderRequests', { contentTypeId: newSelectedContentType.contentTypeId });
    }
  }

  render(){
    const { selectedContentTypeIndex, busy, selectedContentTypes,  defaultOptions, history} = this.props;
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
      <View style={{ flex: 1 }}>
        <PagingListView
          ref={c=> {this.pagingListView = c;}}
          daoName='orderRequestDao'
          history={history}
          dataPath={['driver', 'orders']}
          rowView={RowView}
          options={defaultOptions}
          paginationWaitingView={Paging}
          emptyView={NoItems}
          pageSize={10}
          headerView={() => null}
        />
      </View>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const {dispatch} = initialProps;
  const user = getDaoState(state, ['user'], 'userDao');
  const contentTypes = getDaoState(state, ['contentTypes'], 'contentTypeDao') || [];
  const navigationProps = getNavigationProps(initialProps) || [];
 
  const selectedContentTypeOptions = JSON.parse(user.selectedContentTypes);
  const selectedContentTypeIds = selectedContentTypeOptions ? Object.keys(selectedContentTypeOptions).filter(c => !!selectedContentTypeOptions[c]).map( key => parseInt(key, 10)) : [];
  const selectedContentTypes = contentTypes.filter(ct => !!~selectedContentTypeIds.indexOf(ct.contentTypeId));
  let { contentTypeId } = navigationProps;
  contentTypeId = contentTypeId || (selectedContentTypes[0] && selectedContentTypes[0].contentTypeId);
  const selectedContentType = contentTypes.find(ct => ct.contentTypeId === contentTypeId) || selectedContentTypes[0];
  const selectedContentTypeIndex = selectedContentTypes.indexOf(selectedContentType);
  const contentTypeOptions = contentTypeId ? selectedContentTypeOptions[contentTypeId] : {};

  const defaultOptions = {
    ...DRIVER_ORDER_REQUEST_DEFAULT_OPTIONS,
    contentType: selectedContentType,
    contentTypeOptions
  };

  const resetOrderRequests = () => {
    dispatch(resetSubscriptionAction('orderRequestDao', defaultOptions));
  };

  return {
    ...initialProps,
    resetOrderRequests,
    defaultOptions,
    selectedContentTypes,
    contentTypeOptions,
    contentTypeId,
    selectedContentType,
    selectedContentTypeIndex,
    isDelivery: navigationProps.isDelivery !== undefined ? navigationProps.isDelivery : true,
    busy: isAnyLoading(state, ['vehicleDao', 'userDao']) || !selectedContentType,
    user
  };
};

export default connect(
  mapStateToProps
)(DriverOrderRequests);
