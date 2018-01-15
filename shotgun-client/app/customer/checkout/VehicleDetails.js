import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {Icon, Button, Container, Header, Text, Title, Body, Left, Grid, Row, Col, Content} from 'native-base';
import {getDaoState} from 'common/dao';
import {merge} from 'lodash';
import { withRouter } from 'react-router';

class VehicleDetails extends Component {
  constructor(props) {
    super(props);
  }

  onChangeValue(field, value) {
    const {context} = this.props;
    const {delivery} = context.state;
    context.setState({delivery: merge({}, delivery, {[field]: value})});
  }


  renderVehicleType(vehicleType, style = {}){
    return <Col style={style}>
      <Row><Button onPress={() => this.onChangeValue('vehicleTypeId', vehicleType.vehicleTypeId)} large><Icon style={styles.productSelectIcon} name='car'/></Button></Row>
      <Row style={styles.vehicleSelectTextRow}><Text style={styles.vehicleSelectText}>{vehicleType.description}</Text></Row>
    </Col>;
  }

  render() {
    const {history, vehicleTypePairs} = this.props;
 
    return <Container>
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
          {vehicleTypePairs.map(
            ([left, right], idx) =>
              <Row key={idx}>
                {this.renderVehicleType(left, {paddingRight: 25})}
                {this.renderVehicleType(right)}
              </Row>
          )}
        </Grid>
        <Button fullWidth iconRight onPress={() =>  history.push('/Customer/Checkout/ItemDetails')}>
          <Text uppercase={false}>Continue</Text>
          <Icon name='arrow-forward'/>
        </Button>
      </Content>
    </Container>;
  }
}

const styles = {
  subTitle: {
    marginTop: 25,
    marginBottom: 30
  },
  vehicleSelectTextRow: {
    justifyContent: 'center'
  },
  vehicleSelectText: {
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  }
};

VehicleDetails.PropTypes = {
  user: PropTypes.object
};


const mapStateToProps = (state, initialProps) => {
  const vehicleTypes = getDaoState(state, ['vehicleTypes'], 'vehicleTypeDao');
  const vehicleTypePairs = vehicleTypes ? vehicleTypes.reduce((result, value, index, array) => {
    if (index % 2 === 0){
      result.push(array.slice(index, index + 2));
    }
    return result;
  }, []) : [];

  return {
    vehicleTypes,
    vehicleTypePairs,
    user: getDaoState(state, ['user'], 'userDao'),
    ...initialProps
  };
};

export default withRouter(connect(
  mapStateToProps
)(VehicleDetails));


