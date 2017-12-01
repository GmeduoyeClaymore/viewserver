import React from 'react';
import {Text, Button, Container} from 'native-base';

export default RegistrationCommon = ({history}) => {
  return <Container>
    <Text>Sign up as...</Text>
    <Button onPress={() => history.push('/Customer/Registration')}><Text>Customer</Text></Button>
    <Button onPress={() => history.push('/Driver/Registration')}><Text>Driver</Text></Button>
  </Container>;
};
