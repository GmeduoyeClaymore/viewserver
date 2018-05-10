import React, {Component}  from 'react';
import {withExternalState} from 'custom-redux';
import { Container, Button, Text, Grid, Col, Row, Item, Input} from 'native-base';
import {ErrorRegion, Icon} from 'common/components';
import { getDaoState } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import {addressToText} from 'common/components/maps/MapUtils';
import {UserRelationshipsControl} from 'common/components/relationships/UserRelationships';

class UsersForProductMap extends Component{
  onChangeText = async(location, field, value) => {
    const {order} = this.props;
    const currentLocation = order[location];
    this.setState({order: {...order, [location]: {...currentLocation, [field]: value}}});
  }

  getLocationText = (address, addressKey, placeholder) => {
    return  <Item style={styles.inputRow} onPress={() => this.doAddressLookup(placeholder, addressKey)}>
      <Icon name="pin" style={styles.inputPin} originPin />
      {address && address.line1 !== undefined ? <Col size={30}>
        <Input placeholder='flat/business' style={styles.flatInput} value={address.flatNumber} placeholderTextColor={shotgun.silver} onChangeText={(value) => this.onChangeText(addressKey, 'flatNumber', value)} validationSchema={validationSchema.flatNumber} maxLength={30}/>
      </Col> : null}
      <Col size={70}>
        <Text numberOfLines={1} style={address && address.line1 ? {} : styles.locationTextPlaceholder}>{addressToText(address) || placeholder}</Text>
      </Col>
    </Item>;
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
    const {order, errors, next, client, history} = this.props;
    const {origin} = order;
    const disableDoneButton = !origin || origin.line1 == undefined;

    return <Container>
      <Button transparent style={styles.backButton} onPress={() => history.goBack()} >
        <Icon name='back-arrow'/>
      </Button>
      <Grid>
        <Row>
          <Row style={styles.inputRowHolder}>
            <Col>
              {this.getLocationText(origin, 'origin', 'Enter job location')}
            </Col>
          </Row>
          <UserRelationshipsControl {...this.props} client={client} order={order} onPressAssignUser={this.assignDeliveryToUser}/>
          <ErrorRegion errors={errors} />
        </Row>
      </Grid>
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
  inputRowHolder: {
    position: 'absolute',
    top: 65,
    left: 15,
    right: 15,
    zIndex: 2
  },
  inputRow: {
    backgroundColor: shotgun.brandPrimary,
    padding: 10
  },
  backButton: {
    position: 'absolute',
    left: 0,
    top: 6,
    zIndex: 2
  },
  flatInput: {
    padding: 0,
    height: 12,
    lineHeight: 18
  },
  inputPin: {
    paddingRight: 15
  },
  locationTextPlaceholder: {
    color: shotgun.silver
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


