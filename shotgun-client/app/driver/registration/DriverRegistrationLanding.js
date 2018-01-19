import React from 'react';
import {Text, Button, Container, Grid, Col, Row} from 'native-base';

export default DriverRegistrationLanding = ({history}) => {
  return <Container padded>
    <Grid>
      <Row size={50}>
        <Col>
          <Text>Big Image Here</Text>
        </Col>
      </Row>
      <Row size={50}>
        <Col>
          <Button fullWidth style={styles.signInButton} onPress={() => history.push('/Driver/Registration/Login')}><Text uppercase={false}>Sign in</Text></Button>
          <Button fullWidth light onPress={() => history.push('/Driver/Registration/UserDetails')}><Text uppercase={false}>I'm new, register</Text></Button>
        </Col>
      </Row>
    </Grid>
  </Container>;
};

const styles = {
  signInButton: {
    marginBottom: 15
  }
};
