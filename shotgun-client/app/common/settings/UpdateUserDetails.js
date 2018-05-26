import React, {Component} from 'react';
import {Grid, Row, Col, Text, Content, Header, Body, Container, Title, Item, Label, Left, Button} from 'native-base';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, Icon, ErrorRegion, ImageSelector} from 'common/components';
import {withExternalState} from 'custom-redux';
import {getDaoState, isAnyOperationPending, getOperationErrors} from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {Image} from 'react-native';

class UpdateUserDetails extends Component{
  onChangeText = (field, value) => {
    const {unsavedUser} = this.props;
    this.setState({unsavedUser: {...unsavedUser, [field]: value}});
  }

  onSelectImage = (response) => {
    this.onChangeText('imageData', response.data);
  };

  showPicker = () => {
    ImageSelector.show({title: 'Select Image', onSelect: this.onSelectImage, options: imagePickerOptions});
  };

  onUpdateDetails = () => {
    const {dispatch, unsavedUser, onUpdate} = this.props;
    dispatch(onUpdate(unsavedUser));
  };

  render() {
    const {unsavedUser, busy, errors, history} = this.props;
    const partnerImage = unsavedUser.imageData !== undefined ? `data:image/jpeg;base64,${unsavedUser.imageData}` : unsavedUser.imageUrl;
    const isPartner = unsavedUser.type == 'partner';

    return <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => history.goBack()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Your Details</Title></Body>
      </Header>
      <Content keyboardShouldPersistTaps="always">
        <Grid>
          <Row>
            <Col width={isPartner ? 40 : 100}>
              <Row>
                <Col>
                  <Item stackedLabel>
                    <Label>First name</Label>
                    <ValidatingInput bold value={unsavedUser.firstName} placeholder="John"
                      validateOnMount={unsavedUser.firstName !== undefined}
                      onChangeText={(value) => this.onChangeText('firstName', value)}
                      validationSchema={validationSchema.firstName} maxLength={30}/>
                  </Item>
                </Col>
              </Row>
              <Row>
                <Col>
                  <Item stackedLabel>
                    <Label>Last name</Label>
                    <ValidatingInput bold value={unsavedUser.lastName} placeholder="Smith"
                      validateOnMount={unsavedUser.lastName !== undefined}
                      onChangeText={(value) => this.onChangeText('lastName', value)}
                      validationSchema={validationSchema.lastName} maxLength={30}/>
                  </Item>
                </Col>
              </Row>
            </Col>
            {isPartner ? <Col width={60}><Row>
              <Col >
                {partnerImage != undefined ?
                  <Row style={styles.imageRow} onPress={this.showPicker}>
                    <Image source={{uri: partnerImage}} resizeMode='contain' style={styles.image}/>
                    <Icon style={styles.editPhotoIcon} name='cog'/>
                  </Row> :
                  <Row style={styles.imageRow}>
                    <Button photoButton style={styles.imageButton} onPress={this.showPicker}>
                      <Icon name='camera' style={styles.cameraIcon}/>
                    </Button>
                  </Row>}
              </Col>
            </Row></Col> : null }
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Phone number</Label>
                <ValidatingInput bold keyboardType='phone-pad' placeholder="01234 56678"
                  validateOnMount={unsavedUser.contactNo !== undefined} value={unsavedUser.contactNo}
                  onChangeText={(value) => this.onChangeText('contactNo', value)}
                  validationSchema={validationSchema.contactNo}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Email</Label>
                <ValidatingInput bold keyboardType='email-address' placeholder="email@email.com"
                  validateOnMount={unsavedUser.email !== undefined} value={unsavedUser.email}
                  onChangeText={(value) => this.onChangeText('email', value)}
                  validationSchema={validationSchema.email} maxLength={30}/>
              </Item>
            </Col>
          </Row>
        </Grid>
        <ValidatingButton padded fullWidth iconRight validateOnMount={true} busy={busy}
          onPress={this.onUpdateDetails} validationSchema={yup.object(validationSchema)} model={unsavedUser}>
          <Text uppercase={false}>Update details</Text>
        </ValidatingButton>
        <ErrorRegion errors={errors}/>
      </Content>
   
    </Container>;
  }
}

const styles = {
  imageRow: {
    justifyContent: 'flex-end',
    marginRight: shotgun.contentPadding
  },
  image: {
    aspectRatio: 1,
    width: 100
  },
  imageButton: {
    width: 100,
    height: 100,
    alignItems: 'center',
    flexDirection: 'column',
    borderRadius: 80
  },
  cameraIcon: {
    fontSize: 38
  },
  editPhotoIcon: {
    position: 'absolute',
    top: 70,
    right: 10,
    color: shotgun.brandDark,
    fontSize: 20,
    backgroundColor: shotgun.brandSecondary,
    borderRadius: 25,
    padding: 3
  }
};

const imagePickerOptions = {
  cropping: true,
  cropperCircleOverlay: true,
  useFrontCamera: true,
  height: 400,
  width: 400
};

const validationSchema = {
  firstName: yup.string().required().max(30),
  lastName: yup.string().required().max(30),
  email: yup.string().required().email().max(100),
  contactNo: yup.string().required().matches(/^(((\+?44\s?\d{4}|\(?0\d{4}\)?)\s?\d{3}\s?\d{3})|((\+?44\s?\d{3}|0\d{3})\s?\d{3}\s?\d{4})|((\+?44\s?\d{2}|0\d{2})\s?\d{4}\s?\d{4}))?$/).max(35),
};

const mapStateToProps = (state, initialProps) => {
  const user = getDaoState(state, ['user'], 'userDao');
  let {unsavedUser} = initialProps;
  unsavedUser = unsavedUser !== undefined ? unsavedUser : (user !== undefined ? user : {});
  
  return {
    ...initialProps,
    unsavedUser,
    errors: getOperationErrors(state, [{customerDao: 'updateCustomer'}, {partnerDao: 'updatePartner'}]),
    busy: isAnyOperationPending(state, [{customerDao: 'updateCustomer'}, {partnerDao: 'updatePartner'}])
  };
};

export default withExternalState(mapStateToProps)(UpdateUserDetails);
