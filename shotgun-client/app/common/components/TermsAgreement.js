import React, {Component} from 'react';
import {Linking} from 'react-native';
import {Text} from 'native-base';
import {connect} from 'react-redux';
import shotgun from 'native-base-theme/variables/shotgun';
import {withRouter} from 'react-router';

class TermsAgreement extends Component {
  constructor() {
    super();
  }

  render() {
    const {history} = this.props;

    return <Text style={styles.text}>By registering your account, you agree to our
      <Text style={styles.link} onPress={() => history.push('/TermsAndConditions')}> Services Agreement</Text> and the
      <Text style={styles.link} onPress={() => Linking.openURL('https://stripe.com/gb/connect-account/legal')}> Stripe Connected Account Agreement</Text></Text>;
  }
}

const styles = {
  text: {
    fontSize: 8,
    marginLeft: shotgun.contentPadding,
    marginRight: shotgun.contentPadding,
    marginBottom: shotgun.contentPadding,
  },
  link: {
    fontSize: 8,
    color: shotgun.brandSecondary
  }
};

export default withRouter(connect()(TermsAgreement));
