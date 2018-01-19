import React from 'react';
import {connect} from 'react-redux';
import {Button, Icon, Footer, FooterTab} from 'native-base';
import { withRouter } from 'react-router';

const CustomerMenuBar = ({history, match}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/ProductSelect`)}><Icon name='home'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/CustomerOrders`)}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/CustomerSettings`)}><Icon name='settings'/></Button></FooterTab>
  </Footer>;
}

export default withRouter(connect()(CustomerMenuBar));

