import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {withRouter} from 'react-router';
import {Icon} from 'common/components';

const CustomerMenuBar = ({history, match}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/Checkout`)}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/CustomerOrders`)}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/Settings/CustomerSettings`)}><Icon name='one-person'/></Button></FooterTab>
  </Footer>;
};

export default withRouter(connect()(CustomerMenuBar));

