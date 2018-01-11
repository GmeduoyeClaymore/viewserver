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

  render() {
    const {history} = this.props;

    return <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Vehicle Details</Title></Body>
      </Header>
      <Content padded>
        <Text style={styles.subTitle}>Select the type of vehicle you think you will need for your delivery</Text>
        <Grid>
          <Row>
            <Col style={{paddingRight: 25}}>
              <Row><Button large><Icon style={styles.productSelectIcon} name='car'/></Button></Row>
              <Row style={styles.vehicleSelectTextRow}><Text style={styles.vehicleSelectText}>Small van</Text></Row>
            </Col>
            <Col>
              <Row><Button large><Icon style={styles.productSelectIcon} name='car'/></Button></Row>
              <Row style={styles.vehicleSelectTextRow}><Text style={styles.vehicleSelectText}>Medium van</Text></Row>
            </Col>
          </Row>
          <Row>
            <Col style={{paddingRight: 25}}>
              <Row><Button large><Icon name='car'/></Button></Row>
              <Row style={styles.vehicleSelectTextRow}><Text style={styles.vehicleSelectText}>Large van</Text></Row>
            </Col>
            <Col>
              <Row><Button large><Icon name='car'/></Button></Row>
              <Row style={styles.vehicleSelectTextRow}><Text style={styles.vehicleSelectText}>Tail lift truck</Text></Row>
            </Col>
          </Row>
        </Grid>
      </Content>
      <Button fullWidth iconRight paddedBottom onPress={() =>  history.push('/Customer/Checkout/ItemDetails')}>
        <Text uppercase={false}>Continue</Text>
        <Icon name='arrow-forward'/>
      </Button>
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


const mapStateToProps = (state, initialProps) => ({
  user: getDaoState(state, ['user'], 'userDao'),
  ...initialProps
});

export default withRouter(connect(
  mapStateToProps
)(VehicleDetails));


