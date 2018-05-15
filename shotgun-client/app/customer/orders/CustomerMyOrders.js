import React, {Component} from 'react';
import {connect, ReduxRouter, Route} from 'custom-redux';
import {Tabs, LoadingScreen} from 'common/components';
import {Container, Header, Body, Title, Tab} from 'native-base';
import {getDao} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import CustomerMyOrdersListView from './CustomerMyOrdersListView';

class CustomerMyOrders extends Component{
  goToTabNamed = (name) => {
    const {history, path} = this.props;
    history.replace({pathname: `${path}/${name}`});
  }

  render(){
    const {history, path, height, parentPath, isOrdersDaoRegistered, ...rest} = this.props;
    const routerHeight = height - shotgun.tabHeight;

    return <Container>
      <Header hasTabs>
        <Body><Title>My Jobs</Title></Body>
      </Header>
      <Tabs initialPage={history.location.pathname.endsWith('Complete')  ? 1 : 0} page={history.location.pathname.endsWith('Complete')  ? 1 : 0}  {...shotgun.tabsStyle}>
        <Tab heading='Live Jobs' onPress={() => this.goToTabNamed('Live')}/>
        <Tab heading='Complete' onPress={() => this.goToTabNamed('Complete')}/>
      </Tabs>
      {isOrdersDaoRegistered ? <ReduxRouter  name="CustomerOrdersRouter" {...rest}  height={routerHeight} path={path} defaultRoute='Live'>
        <Route path={'Live'} parentPath={parentPath} isCompleted={false} component={CustomerMyOrdersListView} />
        <Route path={'Complete'} parentPath={parentPath}  isCompleted={true} component={CustomerMyOrdersListView} />
      </ReduxRouter> : <LoadingScreen text="Waiting for order data.."/>}
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    isOrdersDaoRegistered: !!getDao(state, 'orderSummaryDao')
  };
};

export default connect(mapStateToProps)(CustomerMyOrders);
