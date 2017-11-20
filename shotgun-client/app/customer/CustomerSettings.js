import React from 'react';
import { View } from 'react-native';
import { Button, Text } from 'native-base';
import PrincipalService from '../common/services/PrincipalService';

const CustomerSettings = ({navigation}) => {
    const signOut = async() => {
      await PrincipalService.removeCustomerIdFromDevice();
      navigation.navigate('Root');
    };

  const setId = async() => {
    await PrincipalService.setCustomerIdOnDevice('4BBuxi');
    navigation.navigate('Root');
  };

    return <View>
      <Button onPress={signOut}><Text>Sign Out</Text></Button>
      <Button onPress={setId}><Text>Set Id to 4BBuxi</Text></Button>
    </View>;
};

CustomerSettings.navigationOptions = {header: null};
export default CustomerSettings;


