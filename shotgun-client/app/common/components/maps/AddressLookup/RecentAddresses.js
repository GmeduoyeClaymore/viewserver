import React, {Component} from 'react';
import moment from 'moment';
import {Text, Row, List, View, Col} from 'native-base';
import {Icon} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';

const MAX_RECENT_ADDRESSES = 10;

export default class RecentAddresses extends Component {
  getOrderedAddresses = (addreses) => {
    if (!addreses) {
      return addreses;
    }
    let result = [...addreses];
    result = result.filter(ad => !ad.isDefault);
    result.sort((e1, e2) => moment(e1.created).isBefore(moment(e2.created)));
    result = result.slice(0, MAX_RECENT_ADDRESSES);
    return result;
  };

  render() {
    const {deliveryAddresses, onAddressSelected, homeAddress} = this.props;
    return <Col paddedLeft style={styles.resultsContainer}>
      {homeAddress ? <HomeAddressItem address={homeAddress} onAddressSelected={onAddressSelected}/> : null}
      {deliveryAddresses && deliveryAddresses.length ?
        <Col><Text key='header' style={{marginBottom: 15}} smallText>Recent addresses you have used</Text>
          <List key='list'>{this.getOrderedAddresses(deliveryAddresses).map((ad, idx) => <Address address={ad} key={idx} onAddressSelected={onAddressSelected}/>)}</List></Col> :
        <Text smallText>No recent addresses</Text>}
    </Col>;
  }
}

const HomeAddressItem = ({address = {}, onAddressSelected}) =>
  <Row style={{height: 50, alignContent: 'center'}} onPress={() => onAddressSelected(address)}>
    <Icon name="location" paddedIcon originPin style={{alignSelf: 'flex-start', marginTop: 6}}/>
    <View>
      <Text style={{paddingBottom: 6}}>Home</Text>
      <Text smallText>{`${address.line1}, ${address.postCode}`}</Text>
    </View>
  </Row>;

const styles = {
  resultsContainer: {
    marginTop: 0,
    paddingTop: shotgun.contentPadding
  }
};


