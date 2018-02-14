import React, {Component} from 'react';
import {View} from 'react-native';
import {Text, Content, Header, Body, Container, Title, Item, Label, Left, Button, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, ErrorRegion, Icon} from 'common/components';
import {merge} from 'lodash';
import {connect} from 'custom-redux';
import {withRouter} from 'react-router';
import {isAnyLoading, getLoadingErrors, isAnyOperationPending, getOperationError } from 'common/dao';
import * as ContentTypes from 'common/constants/ContentTypes';
import {registerDriver} from 'driver/actions/DriverActions';
import shotgun from 'native-base-theme/variables/shotgun';

class DeliveryDetails extends Component{
  constructor(props){
    super(props);
    this.onChangeText = this.onChangeText.bind(this);
    this.togglePeopleVisibility = this.togglePeopleVisibility.bind(this);
    this.requestVehicleDetails = this.requestVehicleDetails.bind(this);
    this.register = this.register.bind(this);
    this.state = {
      peopleVisible: false
    };
  }

  onChangeText(field, value){
    const {context} = this.props;
    const {vehicle = {}} = context.state;
    context.setState({vehicle: merge(vehicle, {[field]: value})});
  }

  async register(){
    const {user, vehicle, bankAccount, address, selectedContentTypes, dispatch, history} = this.props;
    user.selectedContentTypes = selectedContentTypes.join(',');
    dispatch(registerDriver(user, vehicle, address, bankAccount, () => history.push('/Root')));
  }

  async requestVehicleDetails(){
    const {context, client, vehicle} = this.props;
    try {
      const vehicleDetails = await client.invokeJSONCommand('vehicleDetailsController', 'getDetails', vehicle.registrationNumber);
      context.setState({vehicle: vehicleDetails, errors: ''});
    } catch (error){
      context.setState({errors: error});
    }
  }


  printDimensions(){
    const {dimensions} = this.props;
    return `${Math.floor(dimensions.height / 1000)}m  x ${Math.floor(dimensions.width / 1000)}m x ${Math.floor(dimensions.length / 1000)}m`;
  }

  togglePeopleVisibility(peopleVisible){
    this.setState({peopleVisible});
  }

  render(){
    const {context, dimensions, errors, busy} = this.props;
    const {vehicle = {}} = context.state;
    const {peopleVisible} = this.state;
    const {numAvailableForOffload} = vehicle;

    return <Container>
      <Content keyboardShouldPersistTaps="always" padded>
        <View>
          <Row>
            <Item stackedLabel style={{marginLeft: 0, borderBottomWidth: 0, width: '100%'}}>
              <Label>Vehicle registration</Label>
              <ValidatingInput bold placeholder='AB01 CDE' value={vehicle.registrationNumber} validateOnMount={vehicle.registrationNumber !== undefined} onChangeText={(value) => this.onChangeText('registrationNumber', value)} validationSchema={validationSchema.registrationNumber} maxLength={10}/>
            </Item>
          </Row>
          <Row>
            <Col>
              <ValidatingButton fullWidth onPress={() => this.requestVehicleDetails()} validateOnMount={vehicle.registrationNumber !== undefined} validationSchema={validationSchema.registrationNumber} model={vehicle.registrationNumber || ''}><Text uppercase={false}>Look up my vehicle</Text></ValidatingButton>
            </Col>
          </Row>
          {vehicle.make != undefined ? (<Row>
            <Col>
              <Row><Item stackedLabel vehicleDetails>
                <Label>Vehicle model</Label>
                <Text>{`${vehicle.make} ${vehicle.model}`} </Text>
              </Item></Row>
              <Row><Item stackedLabel vehicleDetails>
                <Label>Vehicle colour</Label>
                <Text>{vehicle.colour}</Text>
              </Item></Row>
              <Row><Item stackedLabel vehicleDetails>
                <Label>Approx dimensions</Label>
                <Text>{this.printDimensions()}</Text>
                <Text>{`${dimensions.weight}kg Max`}</Text>
              </Item></Row>
            </Col>
          </Row>) : null}
          <Row>
            <Col>
              <Grid style={{marginBottom: shotgun.contentPadding, marginTop: 50}}>
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
                        <Button personButton active={numAvailableForOffload == 1} onPress={() => this.onChangeText('numAvailableForOffload', 1)} >
                          <Icon name='one-person'/>
                        </Button>
                      </Row>
                      <Row style={styles.personSelectTextRow}>
                        <Text style={styles.personSelectText}>1</Text>
                      </Row>
                    </Col>
                    <Col style={{marginRight: 10}}>
                      <Row>
                        <Button personButton active={numAvailableForOffload == 2} onPress={() => this.onChangeText('numAvailableForOffload', 2)} >
                          <Icon name='one-person'/>
                        </Button>
                      </Row>
                      <Row style={styles.personSelectTextRow}>
                        <Text style={styles.personSelectText}>2</Text>
                      </Row>
                    </Col>
                    <Col>
                      <Row>
                        <Button personButton active={numAvailableForOffload == 3} onPress={() => this.onChangeText('numAvailableForOffload', 3)} >
                          <Icon name='one-person'/>
                        </Button>
                      </Row>
                      <Row style={styles.personSelectTextRow}>
                        <Text style={styles.personSelectText}>3</Text>
                      </Row>
                    </Col>
                  </Row>
                </Grid> : null}
            </Col>
          </Row>
        </View>
      </Content>
      <ErrorRegion errors={errors}>
        <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} busy={busy} onPress={this.register} validationSchema={yup.object(validationSchema)} model={vehicle}>
          <Text uppercase={false}>Register</Text>
          <Icon next name='forward-arrow'/>
        </ValidatingButton>
      </ErrorRegion>
    </Container>;
  }
}

const styles = {
  personSelectTextRow: {
    justifyContent: 'center'
  },
  personSelectText: {
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  }
};


const validationSchema = {
  registrationNumber: yup.string().required(), //TODO REGEX DOESNT WORK ON IOS.matches(/ ^([A-Z]{3}\s?(\d{3}|\d{2}|d{1})\s?[A-Z])|([A-Z]\s?(\d{3}|\d{2}|\d{1})\s?[A-Z]{3})|(([A-HK-PRSVWY][A-HJ-PR-Y])\s?([0][2-9]|[1-9][0-9])\s?[A-HJ-PR-Z]{3})$/i),
  colour: yup.string().required(),
  numAvailableForOffload: yup.number().required(),
  make: yup.string().required(),
  model: yup.string().required(),
  dimensions: yup.object().required()
};

const mapStateToProps = (state, initialProps) => {
  const {context = {}} = initialProps;
  const {vehicle = {}, errors = [], selectedContentTypes, user, bankAccount, address} = context.state;
  const {dimensions} = vehicle;
  const registrationErrors = getOperationError(state, 'driverDao', 'registerDriver') || [];
  const loadingErrors = getLoadingErrors(state, ['contentTypeDao']) || [];
  const busy = isAnyOperationPending(state, [{ driverDao: 'registerDriver'}] || isAnyLoading(state, ['userDao', 'vehicleDao', 'driverDao']));
  return {
    ...initialProps,
    context,
    vehicle,
    user,
    bankAccount,
    address,
    selectedContentTypes,
    dimensions,
    busy,
    errors: [loadingErrors, errors, registrationErrors].filter( c=> !!c).join('\n')
  };
};


export default withRouter(connect(mapStateToProps)(DeliveryDetails));
