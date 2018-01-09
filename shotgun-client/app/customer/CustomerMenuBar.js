import React from 'react';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {Button, Icon, Footer, FooterTab} from 'native-base';
import { withRouter } from 'react-router';

const CustomerMenuBar = ({history, match}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/ProductSelect`)}><Icon name='home'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/CustomerOrders`)}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.push(`${match.path}/CustomerSettings`)}><Icon name='settings'/></Button></FooterTab>
  </Footer>;
};

CustomerMenuBar.PropTypes = {
  cart: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(CustomerMenuBar));

