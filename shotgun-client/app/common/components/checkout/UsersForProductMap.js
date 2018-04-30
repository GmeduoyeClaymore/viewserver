import React, {Component}  from 'react';
import {withExternalState} from 'custom-redux';
import { Container, Button, Text, Col, Row, Header, Title, Body, Left} from 'native-base';
import {ErrorRegion, Icon} from 'common/components';
import { getDaoState } from 'common/dao';
import {TextInput, Dimensions} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import {addressToText} from 'common/components/maps/MapUtils';
import {UserRelationshipsControl} from 'common/components/relationships/UserRelationships';
const {width} = Dimensions.get('window');
const contentWidth = width - 20;

const getAddressForlocation = async (client, location) => {
  if (!location || !location.latitude || !location.longitude){
    return undefined;
  }
  const {latitude, longitude} = location;
  const results =  await client.invokeJSONCommand('mapsController', 'getAddressesFromLatLong', {
    latitude, longitude
  });
  return results[0];
};

class UsersForProductMap extends Component{
  constructor(props){
    super(props);
    this.doAddressLookup = this.doAddressLookup.bind(this);
    this.getLocationTextInput = this.getLocationTextInput.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.assignDeliveryToUser = this.assignDeliveryToUser.bind(this);
    this.state = {};
  }

  async componentDidMount(){
    const {me = {}, delivery, client} = this.props;
    const {latitude, longitude} = me;
    
    this.setState({delivery: {...delivery, origin: delivery.origin && delivery.origin.line1 ? delivery.origin : await getAddressForlocation(client, {longitude, latitude})}});
  }

  async onChangeText(location, field, value){
    const {delivery} = this.props;
    newLocation = {...delivery[location], [field]: value};
    this.setState({delivery: {...delivery, [location]: newLocation}});
  }

  getLocationTextInput(address, addressKey, placeholder){
    style = address && address.line1 ? {} : styles.locationTextPlaceholder;
    text = addressToText(address) || placeholder;
    const {onChangeText, doAddressLookup} = this;
    return  <Row>
      {address && address.line1 !== undefined ? <Col size={30}>
        <TextInput placeholder='flat/business'  multiline={false} style={{paddingTop: 0, textAlignVertical: 'top'}} underlineColorAndroid='transparent' placeholderTextColor={shotgun.silver} value={address.flatNumber}  onChangeText={(value) => onChangeText(addressKey, 'flatNumber', value)} validationSchema={validationSchema.flatNumber} maxLength={10}/>
      </Col> : null}
      <Col size={70}>
        <Text style={style} onPress={() => doAddressLookup(placeholder, addressKey)}>{text}</Text>
      </Col>
    </Row>;
  }

  assignDeliveryToUser(user){
    const {delivery: oldDelivery, next, history}  = this.props;
    const delivery = {...oldDelivery};
    delivery.partnerId = user.userId;
    this.setState({delivery, deliveryUser: user}, () => history.push(next));
  }


  doAddressLookup(addressLabel, addressKey){
    const {history, parentPath} = this.props;
    history.push(`${parentPath}/AddressLookup`, {addressLabel, addressPath: ['delivery', addressKey]});
  }


  render(){
    const {getLocationTextInput, assignDeliveryToUser} = this;
    const {origin, selectedProduct = {}, errors, disableDoneButton, next, deliveryUser, client, history} = this.props;
    const title = deliveryUser ? `Assigned to ${deliveryUser.firstName} ${deliveryUser.lastName}  (${selectedProduct.name})` : `${selectedProduct.name}s`;
    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>{title}</Title></Body>
      </Header>
      <Row style={{paddingLeft: 10}}>
        <Icon name="pin" paddedIcon originPin />
        {getLocationTextInput(origin, 'origin', 'Enter job location')}
      </Row>
      <Row size={25}>
        <UserRelationshipsControl {...this.props} width={contentWidth}  client={client} geoLocation={origin} selectedProduct={selectedProduct} onPressAssignUser={assignDeliveryToUser}/>
        <ErrorRegion errors={errors} />
      </Row>
      <Button fullWidth paddedBottom iconRight onPress={() => history.push(next)} disabled={disableDoneButton}>
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
  const {delivery, selectedContentType, selectedProduct, selectedUser, selectedUserIndex} = initialProps;
  const {origin} = delivery;
  const disableDoneButton = !origin || origin.line1 == undefined;

  return {
    ...initialProps,
    selectedUser,
    selectedUserIndex,
    me: getDaoState(state, ['user'], 'userDao'),
    delivery, selectedProduct, selectedContentType, origin,  disableDoneButton
  };
};

export default withExternalState(mapStateToProps)(UsersForProductMap);


