import React, {Component} from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {View, Text} from 'react-native';
import PagingListView from 'common/components/PagingListView';
import { withRouter } from 'react-router';
import {Container, Content, Spinner, Header, Body, Title, Tab, List} from 'native-base';
import Tabs from 'common/components/Tabs';
import {isAnyLoading} from 'common/dao';

import shotgun from 'native-base-theme/variables/shotgun';
import OrderRequest from 'common/components/OrderRequest';
import CustomerOrderCta from './components/CustomerOrderCta';

class CustomerOrders extends Component{
  constructor(props){
    super(props);
    this.state = {isCompleted: false};
  }
    
  setIsCompleted(isCompleted){
    if (this.state.isCompleted !== isCompleted) {
      this.setState({isCompleted});
    }
  }

  render(){
    const {isCompleted} = this.state;

    const reportOptions = {
      isCompleted,
      columnsToSort: [{ name: 'eta', direction: 'asc' }],
      reportId: 'customerOrderSummary'}

    const Paging = () => <View style={{flex: 1}}><Spinner /></View>;
    const NoItems = () => <View style={{flex: 1, display: 'flex'}}><Text>No orders to display</Text></View>;
    const RowView = (orderSummary) => [<OrderRequest orderSummary={orderSummary} key={orderSummary.orderId} next='/Customer/CustomerOrderDetail'/>, <CustomerOrderCta key={`${orderSummary.orderId}Cta`} orderSummary={orderSummary}/>];

    return <Container>
      <Header hasTabs>
        <Body><Title>My Jobs</Title></Body>
      </Header>
      <Tabs initialPage={isCompleted ? 1 : 0} {...shotgun.tabsStyle} onChangeTab={({i}) => this.setIsCompleted(i == 1)}>
        <Tab heading="Live Jobs"/>
        <Tab heading="Complete"/>
      </Tabs>
      <Content>
        <List>
          <PagingListView
            daoName='orderSummaryDao'
            dataPath={['orders']}
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
  }
}

CustomerOrders.PropTypes = {
  customer: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  busy: isAnyLoading(state, ['orderSummaryDao', 'paymentDao']),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(CustomerOrders));
