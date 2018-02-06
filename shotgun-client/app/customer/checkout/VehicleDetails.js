import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'custom-redux';
import {Button, Container, Header, Text, Title, Body, Left, Grid, Row, Content, View} from 'native-base';
import {getDaoState, isAnyLoading} from 'common/dao';
import {merge} from 'lodash';
import { withRouter } from 'react-router';
import yup from 'yup';
import {LoadingScreen, ValidatingButton, Icon} from 'common/components';

class VehicleDetails extends Component {
  constructor(props) {
    super(props);
    this.onChangeValue = this.onChangeValue.bind(this);
  }

  onChangeValue(field, value){
    const {context} = this.props;
    const {delivery} = context.state;
    context.setState({delivery: merge({}, delivery, {[field]: value})});
  }

  renderVehicleType(vehicleType, i){
    const {context} = this.props;
    const {delivery} = context.state;
    return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
      <Button style={{height: 'auto'}} large active={delivery.vehicleTypeId == vehicleType.vehicleTypeId} onPress={() => this.onChangeValue('vehicleTypeId', vehicleType.vehicleTypeId)}>
        <Icon name='small-van'/>
      </Button>
      <Text style={styles.vehicleSelectText}>{vehicleType.description}</Text>
    </View>;
  }

  render() {
    const {navigationStrategy, vehicleTypes, context, busy} = this.props;
    const {delivery} = context.state;

   
    return busy ? <LoadingScreen text="Loading Vehicle Types" /> : <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='back-arrow' onPress={() => navigationStrategy.prev()} />
          </Button>
        </Left>
        <Body><Title>Vehicle Details</Title></Body>
      </Header>
      <Content padded>
        <Text style={styles.subTitle}>Select the type of vehicle you think you will need for your delivery</Text>
        <Grid>
          <Row style={{flexWrap: 'wrap'}}>
            {vehicleTypes.map((v, i) => this.renderVehicleType(v, i))}
          </Row>
        </Grid>
        <ValidatingButton fullWidth paddedLeftRight iconRight onPress={() =>  navigationStrategy.next()} validateOnMount={true} validationSchema={yup.object(validationSchema)} model={delivery}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow'/>
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


