import React, {Component} from 'react';
import {Text, Header, Left, Body, Container, Button, Title, Content, Grid, Row, Col, Item, Label} from 'native-base';
import yup from 'yup';
import {merge} from 'lodash';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import shotgun from 'native-base-theme/variables/shotgun';
import {connect} from 'react-redux';
import {getDaoState, isAnyOperationPending, getOperationError} from 'common/dao';
import ErrorRegion from 'common/components/ErrorRegion';
import { withRouter } from 'react-router';
import {updateDeliveryAddress} from 'common/actions/CommonActions';
import {Icon} from 'common/components/Icon';

class HomeAddressDetails  extends Component{
  constructor(props) {
    super(props);

    this.state = {
      deliveryAddress: {...props.deliveryAddress}
    };
  }

  render() {
    const {history, busy, errors, dispatch, next, match} = this.props;
    const {deliveryAddress} = this.state;

    const onAddressSelected = (address) => {
      history.push(match.path, {selectedAddress: {...address, isDefault: true}});
    };

    const onChangeText = async (field, value) => {
      this.setState({deliveryAddress: merge(deliveryAddress, {[field]: value})});
    };

    const doAddressLookup = (addressLabel) => {
      history.push('/Customer/Settings/AddressLookup', {addressLabel, onAddressSelected});
    };

    const onUpdateAddress = () => {
      dispatch(updateDeliveryAddress(deliveryAddress, () =>  history.push(next)));
    };

    const getLocationText = (location, placeholder) => {
      const style = location.line1 ? {} : styles.locationTextPlaceholder;
      const text = location.line1 ? location.line1 : placeholder;
      return <Text style={[styles.line1Text, style]} onPress={() => doAddressLookup(placeholder)}>{text}</Text>;
    };

    return <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()}/>
          </Button>
        </Left>
        <Body><Title>Update Home Address</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always">
        <Grid>
          <Row>
            <Col>
              <Item stackedLabel first>
                <Label>Street Address</Label>
                {getLocationText(deliveryAddress, 'Search for your home address')}
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Flat number/Business Name</Label>
                <ValidatingInput bold placeholder="Optional" value={deliveryAddress.flatNumber}
                  validateOnMount={deliveryAddress.flatNumber !== undefined}
                  onChangeText={(value) => onChangeText('flatNumber', value)}
                  validationSchema={validationSchema.flatNumber} maxLength={30}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>City</Label>
                <ValidatingInput bold placeholder="Cityville" value={deliveryAddress.city}
                  validateOnMount={deliveryAddress.city !== undefined}
                  onChangeText={(value) => onChangeText('city', value)}
                  validationSchema={validationSchema.city} maxLength={30}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Postcode</Label>
                <ValidatingInput bold placeholder="PC12 ABC" value={deliveryAddress.postCode}
                  validateOnMount={deliveryAddress.postCode !== undefined}
                  onChangeText={(value) => onChangeText('postCode', value)}
                  validationSchema={validationSchema.postCode} maxLength={30}/>
              </Item>
            </Col>
          </Row>
        </Grid>
      </Content>
      <ErrorRegion errors={errors}>
        <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} busy={busy}
          onPress={onUpdateAddress} validationSchema={yup.object(validationSchema)}
          model={deliveryAddress}>
          <Text uppercase={false}>Update Address</Text>
        </ValidatingButton>
      </ErrorRegion>
    </Container>;
  }
}

const validationSchema = {
  flatNumber: yup.string().max(30),
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
  const selectedAddress = initialProps.location && initialProps.location.state ? initialProps.location.state.selectedAddress : undefined;
  const deliveryAddress = selectedAddress !== undefined ? selectedAddress : (deliveryAddresses !== undefined ? deliveryAddresses.find(ad => ad.isDefault) : {});

  return {
    ...initialProps,
    deliveryAddress,
    errors: getOperationError(state, 'deliveryAddressDao', 'addOrUpdateDeliveryAddress'),
    busy: isAnyOperationPending(state, [{deliveryAddressDao: 'addOrUpdateDeliveryAddress'}])
  };
};

export default withRouter(connect(
  mapStateToProps
)(HomeAddressDetails));
