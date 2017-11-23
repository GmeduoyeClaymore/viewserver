import React from 'react';
import { Button, Text, Content } from 'native-base';
import PrincipalService from '../common/services/PrincipalService';

const CustomerSettings = ({history}) => {
  const signOut = async() => {
    await PrincipalService.removeCustomerIdFromDevice();
    history.push('/Root');
  };

  const setId = async() => {
    await PrincipalService.setCustomerIdOnDevice('4BBuxi');
    history.push('/Root');
  };

  return <Content>
    <Button onPress={signOut}><Text>Sign Out</Text></Button>
    <Button onPress={setId}><Text>Set Id to 4BBuxi</Text></Button>
  </Content>;
};

export default CustomerSettings;


