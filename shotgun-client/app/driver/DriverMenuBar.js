import React from 'react';
import {connect} from 'react-redux';
import {Button, Footer, FooterTab} from 'native-base';
import { withRouter } from 'react-router';
import {Icon} from 'common/components';

const DriverMenuBar = ({history}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.push('/Driver/DriverOrderRequests')}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push('/Driver/DriverOrders')}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push('/Driver/Settings')}><Icon name='one-person'/></Button></FooterTab>
  </Footer>;
};

export default withRouter(connect()(DriverMenuBar));

