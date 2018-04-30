import React, {Component} from 'react';
import {Text, Header, Left, Body, Container, Button, Title, Content, Grid, Row, Col, Item, Label} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, Icon, ErrorRegion} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import {getDaoState, isAnyOperationPending, getOperationError} from 'common/dao';
import {updateDeliveryAddress} from 'common/actions/CommonActions';
import { withExternalState } from 'custom-redux';

class HomeAddressDetails  extends Component{
  onChangeText = async (field, value) => {
    const {unSavedDeliveryAddress} = this.props;
    this.setState({unSavedDeliveryAddress: {...unSavedDeliveryAddress, [field]: value}});
  }

  doAddressLookup = (addressLabel) => {
    const {history, parentPath} = this.props;
    history.push(`${parentPath}/AddressLookup`, {addressLabel, addressPath: ['unSavedDeliveryAddress']});
  }

  onUpdateAddress = () => {
    const {history, dispatch, next, unSavedDeliveryAddress} = this.props;

    dispatch(updateDeliveryAddress({...unSavedDeliveryAddress, isDefault: true},
      () =>  {
        history.push(next);
        this.setState({unSavedDeliveryAddress: undefined});
      }
    ));
  }

  getLocationText = (location = {}, placeholder) => {
    const style = location.line1 ? {} : styles.locationTextPlaceholder;
    const text = location.line1 ? location.line1 : placeholder;
    return <Text style={[styles.line1Text, style]} onPress={() => this.doAddressLookup(placeholder)}>{text}</Text>;
  }

  render() {
    const {history, busy, errors, unSavedDeliveryAddress} = this.props;

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Update Home Address</Title></Body>
      </Header>
      {unSavedDeliveryAddress ? <Content keyboardShouldPersistTaps="always">
        <Grid>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Street Address</Label>
                {this.getLocationText(unSavedDeliveryAddress, 'Search for your home address')}
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Flat number/Business Name</Label>
                <ValidatingInput bold placeholder="Optional" value={unSavedDeliveryAddress.flatNumber}
                  validateOnMount={unSavedDeliveryAddress.flatNumber !== undefined}
                  onChangeText={(value) => this.onChangeText('flatNumber', value)}
                  validationSchema={validationSchema.flatNumber} maxLength={30}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>City</Label>
                <ValidatingInput bold placeholder="Cityville" value={unSavedDeliveryAddress.city}
                  validateOnMount={unSavedDeliveryAddress.city !== undefined}
                  onChangeText={(value) => this.onChangeText('city', value)}
                  validationSchema={validationSchema.city} maxLength={30}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Postcode</Label>
                <ValidatingInput bold placeholder="PC12 ABC" value={unSavedDeliveryAddress.postCode}
                  validateOnMount={unSavedDeliveryAddress.postCode !== undefined}
                  onChangeText={(value) => this.onChangeText('postCode', value)}
                  validationSchema={validationSchema.postCode} maxLength={30}/>
              </Item>
            </Col>
          </Row>
        </Grid>
      </Content> : null}
      <ErrorRegion errors={errors}/>
      <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} busy={busy}
        onPress={this.onUpdateAddress} validationSchema={yup.object(validationSchema)}
        model={unSavedDeliveryAddress}>
        <Text uppercase={false}>Update Address</Text>
      </ValidatingButton>
    </Container>;
  }
}

const validationSchema = {
  line1: yup.string().required().max(30),
  city: yup.string().required().max(30),
  postCode: yup.string()
    .matches(/^([A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2})$/i)
    .required()
};

const styles = {
  line1Text: {
    fontWeight: 'bold',
    fontSize: 18,
    paddingTop: 5,
    paddingBottom: 10
  },
  locationTextPlaceholder: {
    color: shotgun.silver
  }
};

const mapStateToProps = (state, initialProps) => {
  const deliveryAddresses = getDaoState(state, ['customer', 'deliveryAddresses'], 'deliveryAddressDao');
  let {unSavedDeliveryAddress} = initialProps;
  unSavedDeliveryAddress = unSavedDeliveryAddress !== undefined ? unSavedDeliveryAddress : (deliveryAddresses !== undefined ? deliveryAddresses.find(ad => ad.isDefault) : {});

  return {
    ...initialProps,
    unSavedDeliveryAddress,
    errors: getOperationError(state, 'deliveryAddressDao', 'addOrUpdateDeliveryAddress'),
    busy: isAnyOperationPending(state, [{deliveryAddressDao: 'addOrUpdateDeliveryAddress'}])
  };
};

export default withExternalState(mapStateToProps)(HomeAddressDetails);
