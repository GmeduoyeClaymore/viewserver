import React, {Component} from 'react';
import {Grid, Row, Col, Text, Content, Header, Body, Container, Title, Item, Label, Left, Button} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, Icon, ErrorRegion, ImageSelector} from 'common/components';
import {merge} from 'lodash';
import {connect} from 'custom-redux';
import {getDaoState, isAnyOperationPending, getOperationErrors} from 'common/dao';
import {withRouter} from 'react-router';
import {Image} from 'react-native';

class UpdateUserDetails extends Component{
  constructor(props) {
    super(props);

    this.state = {
      user: {
        firstName: props.user.firstName,
        lastName: props.user.lastName,
        contactNo: props.user.contactNo,
        email: props.user.email,
        imageUrl: props.user.imageUrl,
        imageData: undefined,
        type: props.user.type
      }
    };

    this.pickerOptions = {
      cropping: true,
      cropperCircleOverlay: true,
      useFrontCamera: true,
      height: 400,
      width: 400
    };
  }

  render() {
    const {dispatch, busy, errors, history, onUpdate} = this.props;
    const {user} = this.state;
    user.imageUrl = user.imageData !== undefined ? `data:image/jpeg;base64,${user.imageData}` : user.imageUrl;

    const isDriver = user.type == 'driver';

    const onChangeText = (field, value) => {
      this.setState({user: merge(user, {[field]: value})});
    };

    const onSelectImage = (response) => {
      onChangeText('imageData', response.data);
    };

    const showPicker = () => {
      ImageSelector.show({title: 'Select Image', onSelect: onSelectImage, options: this.pickerOptions});
    };

    const onUpdateDetails = () => {
      dispatch(onUpdate(user));
    };

    return <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='back-arrow' onPress={() => history.goBack()}/>
          </Button>
        </Left>
        <Body><Title>Your Details</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always">
        <Grid>
          <Row>
            <Col width={isDriver ? 40 : 100}>
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
            </Col>
            {isDriver ? <Col width={60}><Row>
              <Col >
                {user.imageUrl != undefined ? <Row style={{justifyContent: 'center'}} onPress={showPicker}>
                  <Image source={{uri: user.imageUrl}} resizeMode='contain' style={styles.image}/>
                </Row> : null}
              </Col>
            </Row></Col> : null }
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
  },
  image: {
    aspectRatio: 1,
    borderRadius: 150,
    width: 100
  }
};

const validationSchema = {
  firstName: yup.string().required().max(30),
  lastName: yup.string().required().max(30),
  email: yup.string().required().email().max(100),
  contactNo: yup.string().required().matches(/^(((\+?44\s?\d{4}|\(?0\d{4}\)?)\s?\d{3}\s?\d{3})|((\+?44\s?\d{3}|0\d{3})\s?\d{3}\s?\d{4})|((\+?44\s?\d{2}|0\d{2})\s?\d{4}\s?\d{4}))?$/).max(35),
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
