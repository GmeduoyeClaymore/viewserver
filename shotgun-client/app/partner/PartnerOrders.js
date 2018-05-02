import React, {Component} from 'react';
import {Tabs, Icon} from 'common/components';
import {Container, Header, Body, Title, Tab, Left, Button} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import {connect, ReduxRouter, Route} from 'custom-redux';
import PartnerOrderItems from './PartnerOrderItems';

class PartnerOrders extends Component{
  goToTabNamed = (name) => {
    const {history, isCompleted, path, canGoBack} = this.props;
    history.replace({pathname: `${path}/${name}`}, {isCompleted, canGoBack});
  }

  render() {
    const {history, isCustomer, isCompleted, canGoBack, height} = this.props;

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
      <ReduxRouter  name="PartnerOrdersRouter" height={height - shotgun.tabHeight} defaultRoute='Accepted' {...this.props} >
        <Route path={'Accepted'} component={PartnerOrderItems}/>
        <Route path={'Posted'} component={PartnerOrderItems}/>
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

export default connect(mapStateToProps )(PartnerOrders);
