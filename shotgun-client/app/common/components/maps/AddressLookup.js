import React, { Component } from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';
import { Text, Button, Header, Left, Body, Container, Icon, Title, Input, Grid, Row, List, ListItem, View } from 'native-base';
import { connect } from 'custom-redux';
import { getDaoState } from 'common/dao';
import { parseGooglePlacesData } from 'common/components/maps/MapUtils';
import ErrorRegion from 'common/components/ErrorRegion';
import { debounce } from 'lodash';
import shotgun from 'native-base-theme/variables/shotgun';

const MAX_RECENT_ADDRESSES = 10;

class AddressLookup extends Component {
  static propTypes = {
    client: PropTypes.object,
    addressLabel: PropTypes.string.isRequired
  }

  constructor(props) {
    super(props);

    this.state = {
      busy: false,
      addressSearchText: undefined,
      suggestedPlaces: [],
      errors: undefined
    };
  }

  compare(m1, m2) {
    if (m1.isAfter(m2)) {
      return 1;
    }
    if (m2.isAfter(m1)) {
      return -1;
    }
    return 0;
  }

  getOrderedAddresses(addreses) {
    if (!addreses) {
      return addreses;
    }
    let result = [...addreses];
    result = result.filter(ad => !ad.isDefault);
    result.sort((e1, e2) => this.compare(moment(e1.created), moment(e2.created)));
    result = result.slice(0, MAX_RECENT_ADDRESSES);
    return result;
  }


  getHomeAddress(addreses) {
    if (!addreses) {
      return addreses;
    }
    return addreses.find(ad => ad.isDefault) || addreses[0];
  }

  render() {
    const { addressSearchText, suggestedPlaces, busy, errors } = this.state;
    const { deliveryAddresses = [], addressLabel, client, context, history, addressKey } = this.props;
    const orderedAddresses = this.getOrderedAddresses(deliveryAddresses);
    const homeAddress = this.getHomeAddress(deliveryAddresses);

    const searchAutoCompleteSuggestions = debounce(async (value) => {
      try {
        this.setState({ busy: true });
        const responseJSON = await client.invokeJSONCommand('mapsController', 'makeAutoCompleteRequest', {
          input: value,
          language: 'en'
        });
        this.setState({
          suggestedPlaces: responseJSON.predictions,
          busy: false
        });
      } catch (error) {
        this.setState({ error });
      }
    });

    const onAddressChanged = (value) => {
      this.setState({ addressSearchText: value }, () => searchAutoCompleteSuggestions(value));
    };

    const onAddressSelected = (address) => {
      context.setState({ [addressKey]: address }, () => history.push('/Customer/Checkout/DeliveryMap'));
    };

    const onSuggestedPlaceSelected = async (rowData) => {
      try {
        const res = await client.invokeJSONCommand('mapsController', 'mapPlaceRequest', {
          placeid: rowData.place_id,
          language: 'en'
        }).timeoutWithError(5000, 'Place request timed out');

        onAddressSelected(parseGooglePlacesData(res.result));
      } catch (error) {
        this.setState({ error });
      }
    };

    const renderSuggestedPlace = (result, i) => {
      return <ListItem paddedTopBottom key={i} onPress={() => onSuggestedPlaceSelected(result)}>
        <View>
          <Text style={styles.addressText}>{result.structured_formatting.main_text}</Text>
          <Text style={styles.smallText}>{result.structured_formatting.secondary_text}</Text>
        </View>
      </ListItem>;
    };

    const address = (address, i) => <ListItem paddedTopBottom key={i} onPress={() => onAddressSelected(address)}>
      <View>
        <Text style={styles.addressText}>{`${address.line1}, ${address.postCode}`}</Text>
        <Text style={styles.smallText}>{address.city}</Text>
      </View>
    </ListItem>;

    return (
      <Container>
        <Header>
          <Left>
            <Button transparent disabled={busy}>
              <Icon name='close-circle' onPress={() => history.goBack()} />
            </Button>
          </Left>
          <Body><Title style={styles.title}>{addressLabel}</Title></Body>
        </Header>
        <Grid>
          <Row size={10} style={styles.searchContainer}>
            <ErrorRegion errors={errors}>
              <Icon name="pin" paddedIcon originPin style={{ alignSelf: 'center' }} />
              <Input placeholder={addressLabel} value={addressSearchText} onChangeText={onAddressChanged} />
            </ErrorRegion>
          </Row>
          {homeAddress && suggestedPlaces.length == 0 ? <Row size={20}>
            <Container>
              <Text style={styles.smallText} onPress={() => onAddressSelected(homeAddress)}>Home</Text>
            </Container>
          </Row> : null}

          <Row size={90}>
            {deliveryAddresses && deliveryAddresses.length && suggestedPlaces.length == 0 ? <Container paddedLeft style={styles.resultsContainer}>
              <Text style={styles.smallText}>Recent Addresses</Text>
              <List>{orderedAddresses.map(address)}</List>
            </Container> : null}

            {suggestedPlaces.length > 0 ? <Container paddedLeft>
              <Text style={styles.smallText}>Results</Text>
              <List>{suggestedPlaces.map((r, i) => renderSuggestedPlace(r, i))}</List>
            </Container> : null}
          </Row>
        </Grid>
      </Container>
    );
  }
}

const styles = {
  title: {
    fontSize: 16,
    alignSelf: 'center'
  },
  searchContainer: {
    paddingLeft: shotgun.contentPadding
  },
  resultsContainer: {
    borderTopWidth: 1,
    marginTop: 20,
    paddingTop: shotgun.contentPadding,
    borderColor: shotgun.silver
  },
  addressText: {
    fontSize: 16
  },
  smallText: {
    fontSize: 12,
    color: shotgun.brandLight,
  }
};

const mapStateToProps = (state, initialProps) => ({
  ...(initialProps && initialProps.location && initialProps.location.state ? initialProps.location.state : {}),
  ...getDaoState(state, ['customer'], 'deliveryAddressDao'),
  ...initialProps
});

export default connect(mapStateToProps)(AddressLookup);
