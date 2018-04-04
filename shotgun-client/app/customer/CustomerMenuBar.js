import React from 'react';
import {connect} from 'custom-redux';
import {Button, Footer, FooterTab} from 'native-base';
import {Icon} from 'common/components';

const CustomerMenuBar = ({history, path}) => {
  return <Footer>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/Checkout`, transition: 'bottom'})}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/CustomerOrders`, transition: 'bottom'})}><Icon name='jobs'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/UserRelationships`, transition: 'bottom'})}><Icon style={{marginLeft: 10, marginRight: 10}} name='two-people'/></Button></FooterTab>
    <FooterTab><Button transparent onPress={() => history.just({pathname: `${path}/Settings`, transition: 'bottom'})}><Icon name='one-person'/></Button></FooterTab>
  </Footer>;
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps
  };
};

export default connect(mapStateToProps, true, false)(CustomerMenuBar);

