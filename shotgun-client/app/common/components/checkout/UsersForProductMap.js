import React, {Component}  from 'react';
import {withExternalState} from 'custom-redux';
import { Container, Button, Text, Col, Row, Header, Title, Body, Left} from 'native-base';
import {ErrorRegion, Icon} from 'common/components';
import { getDaoState } from 'common/dao';
import {TextInput} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import {addressToText} from 'common/components/maps/MapUtils';
import {UserRelationshipsControl} from 'common/components/relationships/UserRelationships';
const contentWidth = shotgun.deviceWidth - 20;

class UsersForProductMap extends Component{
  onChangeText = async(location, field, value) => {
    const {order} = this.props;
    const currentLocation = order[location];
    this.setState({order: {...order, [location]: {...currentLocation, [field]: value}}});
  }

  getLocationTextInput = (address, addressKey, placeholder) => {
    return  <Row>
      {address && address.line1 !== undefined ? <Col size={30}>
        <TextInput placeholder='flat/business'  multiline={false} style={{paddingTop: 0, textAlignVertical: 'top'}} underlineColorAndroid='transparent' placeholderTextColor={shotgun.silver} value={address.flatNumber}  onChangeText={(value) => this.onChangeText(addressKey, 'flatNumber', value)} validationSchema={validationSchema.flatNumber} maxLength={10}/>
      </Col> : null}
      <Col size={70}>
        <Text numberOfLines={1} style={address && address.line1 ? {} : styles.locationTextPlaceholder} onPress={() => this.doAddressLookup(placeholder, addressKey)}>{addressToText(address) || placeholder}</Text>
      </Col>
    </Row>;
  }

  assignDeliveryToUser = (deliveryUser) => {
    const {order, next, history}  = this.props;
    this.setState({order: {...order, partnerId: deliveryUser.userId},  deliveryUser}, () => history.push(next));
  }

  doAddressLookup = (addressLabel, addressKey) => {
    const {history, parentPath} = this.props;
    history.push(`${parentPath}/AddressLookup`, {addressLabel, addressPath: ['order', addressKey]});
  }

  render(){
    const {getLocationTextInput, assignDeliveryToUser} = this;
    const {order, errors, next, deliveryUser, client, history} = this.props;
    const {origin, orderProduct} = order;
    const disableDoneButton = !origin || origin.line1 == undefined;

    const title = deliveryUser ? `Assigned to ${deliveryUser.firstName} ${deliveryUser.lastName}  (${orderProduct.name})` : `${orderProduct.name}s`;
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
        <UserRelationshipsControl {...this.props} width={contentWidth}  client={client} geoLocation={origin} selectedProduct={orderProduct} onPressAssignUser={assignDeliveryToUser}/>
        <ErrorRegion errors={errors} />
      </Row>
      <Button fullWidth paddedBottomLeftRight iconRight onPress={() => history.push(next)} disabled={disableDoneButton}>
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
  const {order, selectedContentType, selectedUser, selectedUserIndex} = initialProps;

  return {
    ...initialProps,
    selectedUser,
    selectedUserIndex,
    me: getDaoState(state, ['user'], 'userDao'),
    order,
    selectedContentType
  };
};

export default withExternalState(mapStateToProps)(UsersForProductMap);


