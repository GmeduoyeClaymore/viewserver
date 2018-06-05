import React from 'react';
import {View, Text, Row} from 'native-base';
import {Linking} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';


export default PaymentInfo = ({chargePercentage}) => {
  return <View padder>
    <Row><Text style={{...styles.text, paddingRight: 7}}>{'\u2022'}</Text><Text style={styles.text}>Your payment details will be stored securely by our payment provider <Text style={styles.link} onPress={() => Linking.openURL('https://stripe.com/gb/connect-account/legal')}>Stripe</Text></Text></Row>
    <Row><Text style={{...styles.text, paddingRight: 7}}>{'\u2022'}</Text><Text style={styles.text}>You pay for a job only when the job or stage has been completed.</Text></Row>
    <Row><Text style={{...styles.text, paddingRight: 7}}>{'\u2022'}</Text><Text style={styles.text}>When being paid for a job we transfer the money to you weekly on a Monday.</Text></Row>
    <Row><Text style={{...styles.text, paddingRight: 7}}>{'\u2022'}</Text><Text style={styles.text}>When being paid for a job we will take {chargePercentage}% of the total amount as our fee.</Text></Row>
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

