import React from 'react';
import {Text, Content, Button, H1, Grid, Row, Icon, Col, View} from 'native-base';
import {merge} from 'lodash';
import Products from 'common/constants/Products';
import shotgun from 'native-base-theme/variables/shotgun';

const ProductSelect = ({context, history}) => {
  const selectProduct = (productId) => {
    context.setState({orderItem: merge({}, context.state.orderItem, {productId})});
    history.push('/Customer/Checkout/DeliveryMap');
  };

  return (
    <Content padded contentContainerStyle={styles.container}>
      <View style={styles.titleView}>
        <H1 style={styles.h1}>Start a new job</H1>
        <Text style={styles.subTitle}>What kind of service do you need?</Text>
      </View>
      <View style={styles.productSelectView}>
        <Grid>
          <Row>
            <Col style={{paddingRight: 25}}>
              <Row><Button large onPress={() => selectProduct(Products.DISPOSAL)}><Icon name='trash'/></Button></Row>
              <Row style={styles.productSelectTextRow}><Text style={styles.productSelectText}>Waste collection</Text></Row>
            </Col>
            <Col>
              <Row><Button large onPress={() => selectProduct(Products.DELIVERY)}><Icon name='car'/></Button></Row>
              <Row style={styles.productSelectTextRow}><Text style={styles.productSelectText}>Schedule delivery</Text></Row>
            </Col>
          </Row>
        </Grid>
      </View>
    </Content>
  );
};

const styles = {
  h1: {
    width: '80%',
    marginBottom: 30
  },
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center'
  },
  titleView: {
    flex: 1,
    justifyContent: 'flex-end'
  },
  productSelectView: {
    flex: 1,
    justifyContent: 'flex-start',
    paddingTop: 30
  },
  subTitle: {
    color: shotgun.brandLight
  },
  productSelectTextRow: {
    justifyContent: 'center'
  },
  productSelectText: {
    fontSize: 18,
    fontWeight: 'bold',
    width: '80%',
    textAlign: 'center'
  }
};

export default ProductSelect;
