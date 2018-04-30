import React, {Component} from 'react';
import {Tabs, Icon} from 'common/components';
import {Container, Header, Body, Title, Tab, Left, Button} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import {connect, ReduxRouter, Route} from 'custom-redux';
import DriverOrderItems from './DriverOrderItems';

class DriverOrders extends Component{
  goToTabNamed = (name) => {
    const {history, isCompleted, path, canGoBack} = this.props;
    history.replace({pathname: `${path}/${name}`}, {isCompleted, canGoBack});
  }

  render() {
    const {history, isCustomer, defaultOptions, isCompleted, canGoBack, parentPath, path, height, ordersRoot} = this.props;

    return <Container>
      <Header hasTabs withButton={canGoBack}>
        {canGoBack ? <Left>
          <Button>
            <Icon name='back-arrow' onPress={() => history.goBack()} />
          </Button>
        </Left> : null }
        <Body><Title>{`My Jobs ${isCompleted ? ' (Completed)' : ''}`}</Title></Body>
      </Header>
      <Tabs initialPage={isCustomer ? 1 : 0}  page={isCustomer ? 1 : 0} {...shotgun.tabsStyle}>
        <Tab heading='Accepted' onPress={() => this.goToTabNamed('Accepted')}/>
        <Tab heading='Posted' onPress={() => this.goToTabNamed('Posted')}/>
      </Tabs>
      <ReduxRouter  name="DriverOrdersRouter" height={height - shotgun.tabHeight} defaultRoute='Accepted' {...{history, isCustomer, defaultOptions, isCompleted: !!isCompleted, parentPath, ordersRoot, path} } >
        <Route path={'Accepted'} component={DriverOrderItems}/>
        <Route path={'Posted'} component={DriverOrderItems}/>
      </ReduxRouter>
    </Container>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const {history, path} = initialProps;

  return {
    isCustomer: history.location.pathname.endsWith(`${path}/Posted`),
    ...initialProps
  };
};

export default connect(mapStateToProps )(DriverOrders);
