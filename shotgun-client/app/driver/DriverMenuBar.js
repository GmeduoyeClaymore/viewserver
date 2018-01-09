import React from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {Button, Icon, Footer, FooterTab} from 'native-base';
import { withRouter } from 'react-router';

const DriverMenuBar = ({history}) => {
  return <Footer>
    <FooterTab><Button transparent dark onPress={() => history.push('/Driver/DriverOrderRequests')}><Icon name='home'/></Button></FooterTab>
    <FooterTab><Button transparent dark onPress={() => history.push('/Driver/DriverOrders')}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent dark onPress={() => history.push('/Driver//DriverSettings')}><Icon name='settings'/></Button></FooterTab>
  </Footer>;
};

DriverMenuBar.PropTypes = {
  cart: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DriverMenuBar));

