import React from 'react';
import { StyleSheet } from 'react-native';
import {connect} from 'react-redux';
import PropTypes from 'prop-types';
import {getDaoState} from 'common/dao';
import {Button, Icon, Text, Spinner, Footer, FooterTab} from 'native-base';
import { withRouter } from 'react-router';

const CustomerMenuBar = ({history, match, summary}) => {
  return <Footer style={styles.container}>
    <FooterTab><Button transparent dark onPress={() => history.push(`${match.path}/ProductCategoryList`)}><Icon name='home'/></Button></FooterTab>
    <FooterTab><Button transparent dark onPress={() => {history.push(`${match.path}/Cart`);}}><Icon name='cart'/>{summary.totalQuantity !== undefined ? <Text>{summary.totalQuantity}</Text> : <Spinner style={styles.spinner}/>}</Button></FooterTab>
    <FooterTab><Button transparent dark onPress={() => history.push(`${match.path}/Orders`)}><Icon name='list'/></Button></FooterTab>
    <FooterTab><Button transparent dark onPress={() => history.push(`${match.path}/CustomerSettings`)}><Icon name='settings'/></Button></FooterTab>
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

CustomerMenuBar.PropTypes = {
  cart: PropTypes.object
};

const mapStateToProps = (state, initialProps) => ({
  cart: getDaoState(state, ['cart'], 'cartItemsDao'),
  summary: getDaoState(state, [], 'cartSummaryDao') || {},
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(CustomerMenuBar));

