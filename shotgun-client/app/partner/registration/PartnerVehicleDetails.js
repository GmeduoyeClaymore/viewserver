import React, {Component} from 'react';
import {Text, Item, Label, Grid, Row, Col, Content} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, ErrorRegion} from 'common/components';
import {withExternalState} from 'custom-redux';
import {isAnyOperationPending, getOperationError, getDaoState} from 'common/dao';
import ValidationService from 'common/services/ValidationService';
import {getVehicleDetails} from 'partner/actions/PartnerActions';
import {isEqual} from 'lodash';

class PartnerVehicleDetails extends Component{
  componentWillMount() {
    const {vehicleDetails} = this.props;

    if (vehicleDetails !== undefined) {
      this.setState({vehicle: vehicleDetails, selectedProductIds: vehicleDetails.selectedProductIds});
    }
  }

  componentWillReceiveProps(newProps){
    const {vehicleDetails} = newProps;
    const {vehicle, vehicleDetails: oldVehicleDetails} = this.props;

    if (!isEqual(vehicleDetails, oldVehicleDetails) || vehicle == undefined){
      this.setState({vehicle: {...vehicleDetails, vehicleId: oldVehicleDetails.vehicleId}, errors: '', selectedProductIds: vehicleDetails.selectedProductIds});
    }
  }

  setRegistrationNumber = (registrationNumber) => {
    this.setState({registrationNumber});
  }

  requestVehicleDetails = () => {
    const {dispatch, registrationNumber} = this.props;
    dispatch(getVehicleDetails(registrationNumber));
  }

  render(){
    const {errors, vehicleDetails, busy, registrationNumber} = this.props;

    return <Content><Grid>
      <Row>
        <Col>
          <Item stackedLabel style={styles.vehicleRegItem}>
            <Label>Vehicle registration</Label>
            <ValidatingInput bold placeholder='AB01 CDE' value={registrationNumber} validateOnMount={registrationNumber !== undefined} onChangeText={(value) => this.setRegistrationNumber(value)} validationSchema={validationSchema.registrationNumber} maxLength={10}/>
          </Item>
          <ValidatingButton fullWidth busy={busy} onPress={this.requestVehicleDetails} validateOnMount={registrationNumber !== undefined} validationSchema={validationSchema.registrationNumber} model={registrationNumber || ''}><Text uppercase={false}>Look up my vehicle</Text></ValidatingButton>
        </Col>
      </Row>

      {vehicleDetails.make != undefined ? (<Row style={{flexWrap: 'wrap'}}>
        <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
          <Label>Registration number</Label>
          <Text>{vehicleDetails.registrationNumber}</Text>
        </Item></Col>
        <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
          <Label>Vehicle model</Label>
          <Text>{`${vehicleDetails.make} ${vehicleDetails.model}`} </Text>
        </Item></Col>
        <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
          <Label>Vehicle colour</Label>
          <Text>{vehicleDetails.colour}</Text>
        </Item></Col>
        <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
          <Label>Approx dimensions</Label>
          <Text>{`${vehicleDetails.volume}m\xB3 load space`}</Text>
          <Text>{`${vehicleDetails.weight}kg Max`}</Text>
        </Item></Col>
      </Row>) : null}
      <ErrorRegion errors={errors}/>
    </Grid></Content>;
  }
}

const validationSchema = {
  registrationNumber: yup.string().required(), //TODO REGEX DOESNT WORK ON IOS.matches(/ ^([A-Z]{3}\s?(\d{3}|\d{2}|d{1})\s?[A-Z])|([A-Z]\s?(\d{3}|\d{2}|\d{1})\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\s?([0][2-9]|[1-9][0-9])\s?[A-HJ-PR-Z]{3})$/i),
  colour: yup.string().required(),
  make: yup.string().required(),
  model: yup.string().required(),
  volume: yup.number().required(),
  weight: yup.number().required()
};

const styles = {
  vehicleRegItem: {
    marginLeft: 0,
    borderBottomWidth: 0,
    width: '100%'
  }
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    vehicleDetails: getDaoState(state, ['vehicleDetails'], 'partnerDao') || getDaoState(state, ['vehicle'], 'vehicleDao') || {},
    errors: getOperationError(state, 'partnerDao', 'getVehicleDetails'),
    busy: isAnyOperationPending(state, [{ partnerDao: 'getVehicleDetails'}])
  };
};

const canSubmit = async (state) => {
  const {vehicle} = state;
  return await ValidationService.validate(vehicle, yup.object(validationSchema));
};

export const ConnectedPartnerVehicleDetails =  withExternalState(mapStateToProps)(PartnerVehicleDetails);
export default {control: ConnectedPartnerVehicleDetails, validator: canSubmit};
