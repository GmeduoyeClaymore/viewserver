import React, {Component}  from 'react';
import { connect, setStateIfIsMounted } from 'custom-redux';
import { Container, Button, Text, Grid, Col, Row} from 'native-base';
import {ErrorRegion, Icon} from 'common/components';
import { withRouter } from 'react-router';
import { getDaoState } from 'common/dao';
import {TextInput} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import {addressToText} from 'common/utils';
import UserRelationships from 'common/components/relationships/UserRelationships';
const getAddressForlocation = (location) => {
  if (!location || !location.latitude || !location.longitude){
    return undefined;
  }
  return {
    line1: 'LOCATION_PLACEHOLDER_ADDRESS' + JSON.stringify(location),
    ...location
  };
};

class UsersForProductMap extends Component{
  constructor(props){
    super(props);
    this.doAddressLookup = this.doAddressLookup.bind(this);
    this.setLocation = this.setLocation.bind(this);
    this.getLocationTextInput = this.getLocationTextInput.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.assignDeliveryToUser = this.assignDeliveryToUser.bind(this);
    this.state = {};
    setStateIfIsMounted(this);
  }

  componentDidMount(){
    const {me = {}, context, delivery} = this.props;
    const {latitude, longitude} = me;
    context.setState({delivery: {...delivery, origin: delivery.origin && delivery.origin.line1 ? delivery.origin : getAddressForlocation({longitude, latitude})}});
  }

  async onChangeText(location, field, value){
    const {delivery, context} = this.props;
    newLocation = {...delivery[location], [field]: value};
    context.setState({delivery: {...delivery, [location]: newLocation}});
  }

  getLocationTextInput(address, addressKey, placeholder){
    style = address && address.line1 ? {} : styles.locationTextPlaceholder;
    text = addressToText(address) || placeholder;
    const {onChangeText, setLocation, doAddressLookup} = this;
    return  <Row>
      {address && address.line1 !== undefined ? <Col size={30}>
        <TextInput placeholder='flat/business'  multiline={false} style={{paddingTop: 0, textAlignVertical: 'top'}} underlineColorAndroid='transparent' placeholderTextColor={shotgun.silver} value={address.flatNumber}  onChangeText={(value) => onChangeText(addressKey, 'flatNumber', value)} validationSchema={validationSchema.flatNumber} maxLength={10}/>
      </Col> : null}
      <Col size={70}>
        <Text style={style} onPress={() => doAddressLookup(placeholder, a => setLocation(a, addressKey))}>{text}</Text>
      </Col>
    </Row>;
  }

  setLocation(address, addressKey){
    const {delivery, history, context} = this.props;
    context.setState({delivery: {...delivery, [addressKey]: address }}, () => history.push('/Customer/Checkout/UsersForProductMap'));
  }

  assignDeliveryToUser(user){
    const {delivery: oldDelivery, context, navigationStrategy}  = this.props;
    const delivery = {...oldDelivery};
    delivery.driverId = user.userId;
    context.setState({delivery, deliveryUser: user}, () => navigationStrategy.next());
  }


  doAddressLookup(addressLabel, onAddressSelected){
    const {history} = this.props;
    history.push('/Customer/Checkout/AddressLookup', {addressLabel, onAddressSelected});
  }

  render(){
    const {getLocationTextInput, assignDeliveryToUser} = this;
    const {origin, selectedProduct, errors, disableDoneButton, navigationStrategy, context} = this.props;
    const {state} = context;
    const {deliveryUser} = state;
    const title = deliveryUser ? `Assigned to ${deliveryUser.firstName} ${deliveryUser.lastName}  (${selectedProduct.name})` : `${selectedProduct.name}s`;

    return <Container>
      <Grid>
        <Row size={89}>
          <UserRelationships context={context} title={title} backAction={navigationStrategy.prev} navigationStrategy={navigationStrategy} geoLocation={origin} selectedProduct={selectedProduct} onPressAssignUser={assignDeliveryToUser}/>
          <ErrorRegion errors={errors} />
        </Row>
        <Row size={9} style={styles.inputRow}>
          <Col>
            <Row>
              <Icon name="pin" paddedIcon originPin />
              {getLocationTextInput(origin, 'origin', 'Enter job location')}
            </Row>
          </Col>
        </Row>
      </Grid>
      <Button fullWidth paddedBottom iconRight onPress={() => navigationStrategy.next()} disabled={disableDoneButton}>
        <Text uppercase={false}>Continue</Text>
        <Icon name='forward-arrow' next/>
      </Button>
    </Container>;
  }
}

const validationSchema = {
  flatNumber: yup.string().max(30)
};

const styles = {
  backButton: {
    position: 'absolute',
    left: 0,
    top: 0
  },
  title: {
    fontWeight: 'bold',
    color: '#848484',
    height: 25,
    fontSize: 12
  },
  locationTextPlaceholder: {
    color: shotgun.silver
  },
  inputRow: {
    padding: shotgun.contentPadding
  }
};

const mapStateToProps = (state, initialProps) => {
  const {context} = initialProps;
  const {delivery, selectedContentType, selectedProduct, selectedUser, selectedUserIndex} = context.state;
  const {origin} = delivery;
  const disableDoneButton = !origin || origin.line1 == undefined;

  return {
    ...initialProps,
    state,
    selectedUser,
    selectedUserIndex,
    me: getDaoState(state, ['user'], 'userDao'),
    delivery, selectedProduct, selectedContentType, origin,  disableDoneButton
  };
};

export default withRouter(connect(
  mapStateToProps
)(UsersForProductMap));


