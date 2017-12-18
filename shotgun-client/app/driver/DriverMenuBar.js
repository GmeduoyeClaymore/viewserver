import React from 'react';
import { StyleSheet } from 'react-native';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {Button, Icon, Footer, FooterTab} from 'native-base';
import { withRouter } from 'react-router';

const DriverMenuBar = ({history, match}) => {
  return <Footer style={styles.container}>
    <FooterTab><Button transparent dark onPress={() => history.push(`${match.path}/ProductCategoryList`)}><Icon name='home'/></Button></FooterTab>
    <FooterTab><Button transparent dark onPress={() => history.push(`${match.path}/Orders`)}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent dark onPress={() => history.push(`${match.path}/DriverSettings`)}><Icon name='settings'/></Button></FooterTab>
  </Footer>;
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#FFFFFF',
    paddingTop: 10,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  }
});

DriverMenuBar.PropTypes = {
  cart: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(DriverMenuBar));

