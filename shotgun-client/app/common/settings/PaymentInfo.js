import React from 'react';
import {View, Text} from 'native-base';
import {Linking} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';

export default PaymentInfo = ({chargePercentage}) => {
  return <View padder>
    <Text style={styles.text}>{'\u2022'} Your payment details will be stored securely by our payment provider <Text style={styles.link} onPress={() => Linking.openURL('https://stripe.com/gb/connect-account/legal')}>Stripe</Text></Text>
    <Text style={styles.text}>{'\u2022'} You pay for a job only when the job or stage has been completed.</Text>
    <Text style={styles.text}>{'\u2022'} When being paid for a job we transfer the money to you weekly on a Monday.</Text>
    <Text style={styles.text}>{'\u2022'} When being paid for a job we will take {chargePercentage}% of the total amount as our fee.</Text>
  </View>;
};

styles = {
  link: {
    color: shotgun.brandSecondary
  },
  text: {
    fontWeight: 'bold',
    paddingTop: 5,
    paddingBottom: 5
  }
};

