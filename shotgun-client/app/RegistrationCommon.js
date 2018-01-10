import React from 'react';
import {Text, Button, Content, Grid, Col, Row, View, H1, Icon} from 'native-base';

export default RegistrationCommon = ({history}) => {
  return <Content padded contentContainerStyle={styles.container}>
    <View style={styles.titleView}>
      <H1 style={styles.h1}>Welcome to Shotgun</H1>
      <Text subTitle>What are you here to do?</Text>
    </View>
    <View style={styles.productSelectView}>
      <Grid>
        <Row>
          <Col size={45}>
            <Row><Button large onPress={() => history.push('/Customer/Registration')}><Icon name='cube'/></Button></Row>
            <Row style={styles.productSelectTextRow}><Text style={styles.productSelectText}>Schedule a delivery</Text></Row>
          </Col>
          <Col size={10}>
            <Row style={styles.orRow}><Text>Or</Text></Row>
            <Row></Row>
          </Col>
          <Col size={45}>
            <Row><Button large onPress={() => history.push('/Driver/Registration')}><Icon name='car'/></Button></Row>
            <Row style={styles.productSelectTextRow}><Text style={styles.productSelectText}>Drive and deliver</Text></Row>
          </Col>
        </Row>
      </Grid>
    </View>
  </Content>;
};

const styles = {
  h1: {
    width: '90%',
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
  orRow: {
    justifyContent: 'center',
    alignItems: 'center'
  },
  productSelectTextRow: {
    borderWidth: 1,
    justifyContent: 'center',
    alignItems: 'flex-start'
  },
  productSelectText: {
    fontSize: 18,
    fontWeight: 'bold',
    width: '80%',
    textAlign: 'center'
  }
};
