import React from 'react';
import {Text, Button, Container, Grid, Col, Row} from 'native-base';
import {Icon} from 'common/components';

export default CustomerRegistrationLanding = ({history}) => {
  return <Container padded>
    <Button transparent style={styles.backButton} onPress={() => history.goBack()} >
      <Icon name='back-arrow'/>
    </Button>
    <Grid>
      <Row size={50}>
        <Col>
          <Text>Big Image Here</Text>
        </Col>
      </Row>
      <Row size={50}>
        <Col>
          <Button fullWidth style={styles.signInButton} onPress={() => history.push('/Customer/Registration/Login')}><Text uppercase={false}>Sign in</Text></Button>
          <Button fullWidth light onPress={() => history.push('/Customer/Registration/UserDetails')}><Text uppercase={false}>I'm new, register</Text></Button>
        </Col>
      </Row>
    </Grid>
  </Container>;
};

const styles = {
  signInButton: {
    marginBottom: 15
  },
  backButton: {
    position: 'absolute',
    left: 0,
    top: 0
  }
};
