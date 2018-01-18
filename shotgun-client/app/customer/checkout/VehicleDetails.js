import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Icon, Button, Container, Header, Text, Title, Body, Left, Grid, Row, Content, View} from 'native-base';
import {getDaoState, isAnyLoading} from 'common/dao';
import {merge} from 'lodash';
import { withRouter } from 'react-router';
import yup from 'yup';
import LoadingScreen from 'common/components/LoadingScreen';
import ValidatingButton from 'common/components/ValidatingButton';

class VehicleDetails extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    const {history, vehicleTypes, context, busy} = this.props;
    const {delivery} = context.state;

    const onChangeValue = (field, value) => {
      context.setState({delivery: merge({}, delivery, {[field]: value})});
    };

    const renderVehicleType = (vehicleType, i) => {
      return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
        <Button style={{height: 'auto'}} large active={delivery.vehicleTypeId == vehicleType.vehicleTypeId} onPress={() => onChangeValue('vehicleTypeId', vehicleType.vehicleTypeId)}>
          <Icon name='car'/>
        </Button>
        <Text style={styles.vehicleSelectText}>{vehicleType.description}</Text>
      </View>;
    };
    return busy ? <LoadingScreen text="Loading Vehicle Types" /> : <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Vehicle Details</Title></Body>
      </Header>
      <Content padded>
        <Text style={styles.subTitle}>Select the type of vehicle you think you will need for your delivery</Text>
        <Grid>
          <Row style={{flexWrap: 'wrap'}}>
            {vehicleTypes.map((v, i) => renderVehicleType(v, i))}
          </Row>
        </Grid>
        <ValidatingButton fullWidth paddedLeftRight iconRight onPress={() =>  history.push('/Customer/Checkout/ItemDetails')} validateOnMount={true} validationSchema={yup.object(validationSchema)} model={delivery}>
          <Text uppercase={false}>Continue</Text>
          <Icon name='arrow-forward'/>
        </ValidatingButton>
      </Content>
    </Container>;
  }
}

const validationSchema = {
  vehicleTypeId: yup.string().required(),
};

const styles = {
  subTitle: {
    marginTop: 25,
    marginBottom: 30
  },
  vehicleSelectText: {
    width: '100%',
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  }
};

VehicleDetails.PropTypes = {
  user: PropTypes.object
};


const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  busy: isAnyLoading(state, ['vehicleTypeDao']),
  vehicleTypes: getDaoState(state, ['vehicleTypes'], 'vehicleTypeDao'),
  user: getDaoState(state, ['user'], 'userDao'),
});

export default withRouter(connect(
  mapStateToProps
)(VehicleDetails));


