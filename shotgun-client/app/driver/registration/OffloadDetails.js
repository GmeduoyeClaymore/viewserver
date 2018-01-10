import React from 'react';
import {Input, Text, Content, Header, Body, Container, Title, Item, Label, Left, Button, Icon, Grid, Row, Col} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';
import {connect} from 'react-redux';
import ErrorRegion from 'common/components/ErrorRegion';
import {withRouter} from 'react-router';
import shotgun from 'native-base-theme/variables/shotgun';
import LoadingScreen from 'common/components/LoadingScreen';
import {registerDriver, driverServicesRegistrationAction} from 'driver/actions/DriverActions';
import {isAnyLoading, isAnyOperationPending, getOperationError} from 'common/dao';

const OffloadDetails  = ({context, history, busy, dispatch}) => {
  const {user, vehicle} = context.state;
  const {numAvailableForOffload} = vehicle;

  const onChangeValue = (field, value) => {
    context.setState({vehicle: merge({}, vehicle, {[field]: value})});
  };

  const register = async () => {
    dispatch(registerDriver(user, vehicle, () => history.push('/Root')));
  };

  return busy ? <LoadingScreen text="Registering You With Shotgun"/> : <Container>
    <Header>
      <Left>
        <Button>
          <Icon name='arrow-back' onPress={() => history.goBack()}/>
        </Button>
      </Left>
      <Body><Title>Delivery Details</Title></Body>
    </Header>
    <Content padded>
      <Grid style={{marginBottom: shotgun.contentPadding}}>
        <Row><Text style={{marginBottom: shotgun.contentPadding}}>Are you able to load and off-load items?</Text></Row>
        <Row>
          <Col style={{paddingRight: shotgun.contentPadding}}><Button fullWidth light active={numAvailableForOffload > 0} onPress={() => onChangeValue('numAvailableForOffload', 1)}><Text uppercase={false}>Yes</Text></Button></Col>
          <Col><Button fullWidth light active={numAvailableForOffload == 0} onPress={() => onChangeValue('numAvailableForOffload', 0)}><Text uppercase={false}>No</Text></Button></Col>
        </Row>
      </Grid>
      {numAvailableForOffload > 0 ?
        <Grid>
          <Row>
            <Text style={{paddingBottom: 15}}>How many people will be available?</Text>
          </Row>
          <Row>
            <Col style={{marginRight: 10}}>
              <Row>
                <Button personButton active={numAvailableForOffload == 1} onPress={() => onChangeValue('numAvailableForOffload', 1)} >
                  <Icon name='man'/>
                </Button>
              </Row>
              <Row style={styles.personSelectTextRow}>
                <Text style={styles.personSelectText}>1</Text>
              </Row>
            </Col>
            <Col style={{marginRight: 10}}>
              <Row>
                <Button personButton active={numAvailableForOffload == 2} onPress={() => onChangeValue('numAvailableForOffload', 2)} >
                  <Icon name='man'/>
                </Button>
              </Row>
              <Row style={styles.personSelectTextRow}>
                <Text style={styles.personSelectText}>2</Text>
              </Row>
            </Col>
            <Col>
              <Row>
                <Button personButton active={numAvailableForOffload == 3} onPress={() => onChangeValue('numAvailableForOffload', 3)} >
                  <Icon name='man'/>
                </Button>
              </Row>
              <Row style={styles.personSelectTextRow}>
                <Text style={styles.personSelectText}>3</Text>
              </Row>
            </Col>
          </Row>
        </Grid> : null}
    </Content>
    <ValidatingButton paddedBottom fullWidth iconRight onPress={register} validationSchema={yup.object(validationSchema)} model={vehicle}>
      <Text uppercase={false}>Register</Text>
      <Icon name='arrow-forward'/>
    </ValidatingButton>
  </Container>;
};

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
  numAvailableForOffload: yup.number().required()
};


const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  errors: getOperationError(state, 'driverDao', 'registerDriver'),
  busy: isAnyOperationPending(state, { driverDao: 'registerDriver'} || isAnyLoading(state, ['userDao', 'vehicleDao', 'driverDao'])),
});

export default withRouter(connect(
  mapStateToProps
)(OffloadDetails));
