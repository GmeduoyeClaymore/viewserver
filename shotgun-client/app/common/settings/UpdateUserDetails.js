import React, {Component} from 'react';
import {Grid, Row, Col, Text, Content, Header, Body, Container, Title, Icon, Item, Label, Left, Button} from 'native-base';
import yup from 'yup';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';
import {merge} from 'lodash';
import {connect} from 'react-redux';
import {getDaoState, isAnyOperationPending, getOperationErrors} from 'common/dao';
import ErrorRegion from 'common/components/ErrorRegion';
import {withRouter} from 'react-router';

class UpdateUserDetails extends Component{
  constructor(props) {
    super(props);

    this.state = {
      user: {
        firstName: props.user.firstName,
        lastName: props.user.lastName,
        contactNo: props.user.contactNo,
        email: props.user.email
      }
    };
  }

  render() {
    const {dispatch, busy, errors, history, onUpdate} = this.props;
    const {user} = this.state;

    const onChangeText = async (field, value) => {
      this.setState({user: merge(user, {[field]: value})});
    };

    const onUpdateDetails = () => {
      dispatch(onUpdate(user));
    };

    return <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()}/>
          </Button>
        </Left>
        <Body><Title>Your Details</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always">
        <Grid>
          <Row>
            <Col>
              <Item stackedLabel first>
                <Label>First name</Label>
                <ValidatingInput bold value={user.firstName} placeholder="John"
                  validateOnMount={user.firstName !== undefined}
                  onChangeText={(value) => onChangeText('firstName', value)}
                  validationSchema={validationSchema.firstName} maxLength={30}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Last name</Label>
                <ValidatingInput bold value={user.lastName} placeholder="Smith"
                  validateOnMount={user.lastName !== undefined}
                  onChangeText={(value) => onChangeText('lastName', value)}
                  validationSchema={validationSchema.lastName} maxLength={30}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Phone number</Label>
                <ValidatingInput bold keyboardType='phone-pad' placeholder="01234 56678"
                  validateOnMount={user.contactNo !== undefined} value={user.contactNo}
                  onChangeText={(value) => onChangeText('contactNo', value)}
                  validationSchema={validationSchema.contactNo}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Email</Label>
                <ValidatingInput bold keyboardType='email-address' placeholder="email@email.com"
                  validateOnMount={user.email !== undefined} value={user.email}
                  onChangeText={(value) => onChangeText('email', value)}
                  validationSchema={validationSchema.email} maxLength={30}/>
              </Item>
            </Col>
          </Row>
        </Grid>
      </Content>
      <ErrorRegion errors={errors}>
        <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} busy={busy} style={styles.continueButton}
          onPress={onUpdateDetails} validationSchema={yup.object(validationSchema)} model={user}>
          <Text uppercase={false}>Update details</Text>
        </ValidatingButton>
      </ErrorRegion>
    </Container>;
  }
}

const styles = {
  continueButton: {
    marginTop: 50
  }
};

const validationSchema = {
  firstName: yup.string().required().max(30),
  lastName: yup.string().required().max(30),
  email: yup.string().required().email().max(100),
  contactNo: yup.string().required().matches(/^(((\+44\s?\d{4}|\(?0\d{4}\)?)\s?\d{3}\s?\d{3})|((\+44\s?\d{3}|0\d{3})\s?\d{3}\s?\d{4})|((\+44\s?\d{2}|0\d{2})\s?\d{4}\s?\d{4}))?$/).max(35),
};

const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  user: getDaoState(state, ['user'], 'userDao'),
  errors: getOperationErrors(state, [{customerDao: 'updateCustomer'}, {driverDao: 'updateDriver'}]),
  busy: isAnyOperationPending(state, [{customerDao: 'updateCustomer'}, {driverDao: 'updateDriver'}])
});

export default withRouter(connect(
  mapStateToProps
)(UpdateUserDetails));
