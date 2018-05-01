import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Item, Label, Button, Grid, Row, Col, Content} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {withExternalState} from 'custom-redux';
import {isAnyOperationPending, getOperationError, getDaoState} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import ValidationService from 'common/services/ValidationService';
import {getVehicleDetails} from 'partner/actions/PartnerActions';

class DriverCapabilityDetails extends Component{
  setRegistrationNumber = (registrationNumber) => {
    this.setState({registrationNumber});
  }

  requestVehicleDetails = () => {
    const {dispatch, registrationNumber} = this.props;
    dispatch(getVehicleDetails(registrationNumber));
  }

  togglePeopleVisibility = (peopleVisible) => {
    this.setState({numAvailableForOffload: peopleVisible ? 1 : undefined});
  }

  setNumAvailableForOffload = (numAvailableForOffload) => {
    this.setState({numAvailableForOffload});
  }

  render(){
    const {errors: errorsFromProps = '', vehicle, numAvailableForOffload, registrationNumber} = this.props;
    const {errors: errorsFromState = ''} = this.state;
    const errors = [errorsFromProps, errorsFromState].filter( c=> !!c).join('\n');
    const peopleVisible = numAvailableForOffload > 0;

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

      {vehicle.make != undefined ? (<Row style={{flexWrap: 'wrap'}}>
        <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
          <Label>Vehicle model</Label>
          <Text>{`${vehicle.make} ${vehicle.model}`} </Text>
        </Item></Col>
        <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
          <Label>Vehicle colour</Label>
          <Text>{vehicle.colour}</Text>
        </Item></Col>
        <Col style={{width: '50%'}}><Item stackedLabel vehicleDetails>
          <Label>Approx dimensions</Label>
          <Text>{`${vehicle.dimensions.volume}m\xB3 load space`}</Text>
          <Text>{`${vehicle.dimensions.weight}kg Max`}</Text>
        </Item></Col>
      </Row>) : null}
      <Row>
        <Col>
          <Grid style={styles.offloadGrid}>
            <Row><Text style={{marginBottom: shotgun.contentPadding}}>Are you able to load and off-load items?</Text></Row>
            <Row>
              <Col style={{paddingRight: shotgun.contentPadding}}><Button fullWidth light active={peopleVisible} onPress={() => this.togglePeopleVisibility(true)}><Text uppercase={false}>Yes</Text></Button></Col>
              <Col><Button fullWidth light active={!peopleVisible} onPress={() => this.togglePeopleVisibility(false)}><Text uppercase={false}>No</Text></Button></Col>
            </Row>
          </Grid>
          {peopleVisible ?
            <Grid>
              <Row>
                <Text style={{paddingBottom: 15}}>How many people will be available?</Text>
              </Row>
              <Row>
                <Col style={{marginRight: 10}}>
                  <Row>
                    <Button personButton active={numAvailableForOffload == 1} onPress={() => this.setNumAvailableForOffload(1)} >
                      <View>
                        <Icon name='one-person'/>
                        <Text style={styles.personSelectText}>1</Text>
                      </View>
                    </Button>
                  </Row>
                </Col>
                <Col style={{marginRight: 10}}>
                  <Row>
                    <Button personButton active={numAvailableForOffload == 2} onPress={() => this.setNumAvailableForOffload(2)} >
                      <View>
                        <Icon name='one-person'/>
                        <Text style={styles.personSelectText}>2</Text>
                      </View>
                    </Button>
                  </Row>
                </Col>
                <Col>
                  <Row>
                    <Button personButton active={numAvailableForOffload == 3} onPress={() => this.setNumAvailableForOffload(3)} >
                      <View>
                        <Icon name='one-person'/>
                        <Text style={styles.personSelectText}>3</Text>
                      </View>
                    </Button>
                  </Row>
                </Col>
              </Row>
            </Grid> : null}
        </Col>
      </Row>
      <ErrorRegion errors={errors}/>
    </Grid></Content>;
  }
}

const validationSchema = {
  registrationNumber: yup.string().required(), //TODO REGEX DOESNT WORK ON IOS.matches(/ ^([A-Z]{3}\s?(\d{3}|\d{2}|d{1})\s?[A-Z])|([A-Z]\s?(\d{3}|\d{2}|\d{1})\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\s?([0][2-9]|[1-9][0-9])\s?[A-HJ-PR-Z]{3})$/i),
  colour: yup.string().required(),
  make: yup.string().required(),
  model: yup.string().required(),
  dimensions: yup.object().required()
};

const styles = {
  personSelectText: {
    marginTop: 5,
    fontWeight: 'normal',
    fontSize: 16,
    textAlign: 'center'
  },
  vehicleRegItem: {
    marginLeft: 0,
    borderBottomWidth: 0,
    width: '100%'
  },
  offloadGrid: {
    marginBottom: shotgun.contentPadding,
    marginTop: 20
  }
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
    vehicle: getDaoState(state, ['vehicleDetails'], 'partnerDao') || initialProps.vehicle,
    errors: getOperationError(state, 'partnerDao', 'getVehicleDetails'),
    busy: isAnyOperationPending(state, [{ partnerDao: 'getVehicleDetails'}])
  };
};

const canSubmit = async (state) => {
  const {vehicle} = state;
  return await ValidationService.validate(vehicle, yup.object(validationSchema));
};

export const ConnectedDriverCapabilityDetails =  withExternalState(mapStateToProps)(DriverCapabilityDetails);
export default {control: ConnectedDriverCapabilityDetails, validator: canSubmit};
