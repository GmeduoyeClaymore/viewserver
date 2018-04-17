import React, {Component} from 'react';
import {Grid, Row, Col, Text, Content, Header, Body, Container, Title, Item, Label, Left, Button} from 'native-base';
import yup from 'yup';
import {Image, ScrollView} from 'react-native';
import shotgun from 'native-base-theme/variables/shotgun';
import {ValidatingInput, ValidatingButton, Icon, ImageSelector} from 'common/components';
import DatePicker from 'common/components/datePicker/DatePicker';
import moment from 'moment';
import {withExternalState} from 'custom-redux';

const datePickerOptions = {
  datePickerModeAndroid: 'spinner',
  mode: 'date',
  titleIOS: 'Select delivery time',
  minimumDate: moment().add(-100, 'years').toDate(),
  maximumDate: moment().add(1, 'years').toDate()
};

const imagePickerOptions = {
  cropping: true,
  cropperCircleOverlay: true,
  useFrontCamera: true,
  height: 400,
  width: 400
};

class UserDetails  extends Component{
  constructor(props){
    super(props);
    this.toggleDatePicker = this.toggleDatePicker.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.onSelectImage = this.onSelectImage.bind(this);
    this.showPicker = this.showPicker.bind(this);
  }

  onChangeText(field, value){
    const {user} = this.props;
    this.setState({user: {...user, [field]: value}});
  }

  onChangeDob(dob){
    this.onChangeText('dob', dob);
    this.toggleDatePicker(false);
  }

  toggleDatePicker(dobIsDatePickerVisible){
    this.setState({dobIsDatePickerVisible});
  }

  onSelectImage(response){
    this.onChangeText('imageData', response.data);
  }

  showPicker(){
    ImageSelector.show({title: 'Select Image', onSelect: this.onSelectImage, options: imagePickerOptions});
  }

  render(){
    const {onChangeText} = this;
    const {history, next, user, dobIsDatePickerVisible} = this.props;
    const isDriver = user.type === 'driver';


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
            <Col width={isDriver ? 40 : 100}>
              <Row>
                <Col>
                  <Item stackedLabel>
                    <Label>First name</Label>
                    <ValidatingInput bold value={user.firstName} placeholder="John" validateOnMount={user.firstName !== undefined} onChangeText={(value) => onChangeText('firstName', value)} validationSchema={validationSchema.firstName} maxLength={30}/>
                  </Item>
                </Col>
              </Row>
              <Row>
                <Col>
                  <Item stackedLabel>
                    <Label>Last name</Label>
                    <ValidatingInput bold value={user.lastName} placeholder="Smith" validateOnMount={user.lastName !== undefined} onChangeText={(value) => onChangeText('lastName', value)} validationSchema={validationSchema.lastName} maxLength={30}/>
                  </Item>
                </Col>
              </Row>
            </Col>
            {isDriver ? <Col width={60}><Row >
              <Col >
                {user.imageData != undefined ? <Row style={{justifyContent: 'center'}} onPress={this.showPicker}>
                  <Image source={{uri: `data:image/jpeg;base64,${user.imageData}`}} resizeMode='contain' style={styles.image}/>
                </Row> : null}
                {user.imageData == undefined ? <Button photoButton style={styles.imageButton} onPress={this.showPicker}>
                  <Grid>
                    <Row style={styles.imageButtonIconRow}>
                      <Icon name='camera' style={{marginBottom: 15}}/>
                    </Row>
                    <Row style={{justifyContent: 'center'}}>
                      <Text uppercase={false} style={styles.imageButtonText}>Add profile picture</Text>
                    </Row>
                  </Grid>
                </Button> : null}
              </Col>
            </Row></Col> : null }
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Phone number</Label>
                <ValidatingInput bold keyboardType='phone-pad' placeholder="01234 56678" validateOnMount={user.contactNo !== undefined} value={user.contactNo} onChangeText={(value) => onChangeText('contactNo', value)} validationSchema={validationSchema.contactNo}/>
              </Item>
            </Col>
          </Row>
          {isDriver ? <Row>
            <Col>
              <Item stackedLabel>
                <Label>DOB</Label>
                <ValidatingInput onPress={() => this.toggleDatePicker(true)} editable={false} bold value={user.dob ? moment(user.dob).format('DD MMM YY') : undefined} placeholder="Select Date Of Birth" validateOnMount={user.dob !== undefined} onChangeText={(value) => onChangeText('dob', value)} validationSchema={drivervalidationSchema.dob} maxLength={30}/>
                <DatePicker isVisible={dobIsDatePickerVisible} cannedDateOptions={[]} onCancel={() => this.toggleDatePicker(false)} onConfirm={(date) => this.onChangeDob(date)} {...datePickerOptions} />
              </Item>
            </Col>
          </Row> : null }
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Email</Label>
                <ValidatingInput bold keyboardType='email-address' placeholder="email@email.com" validateOnMount={user.email !== undefined} value={user.email} onChangeText={(value) => onChangeText('email', value)} validationSchema={validationSchema.email} maxLength={30}/>
              </Item>
            </Col>
          </Row>
          <Row>
            <Col>
              <Item stackedLabel>
                <Label>Create an account password</Label>
                <ValidatingInput bold secureTextEntry={true} returnKeyType={'next'} placeholder="****" value={user.password} validateOnMount={user.password !== undefined} onChangeText={(value) => this.onChangeText('password', value)} validationSchema={validationSchema.password} maxLength={30}/>
              </Item>
            </Col>
          </Row>
        </Grid>
      </Content>
      <ValidatingButton paddedBottom fullWidth iconRight validateOnMount={true} onPress={() => history.push(next)} validationSchema={yup.object(isDriver ? drivervalidationSchema : validationSchema)} model={user}>
        <Text uppercase={false}>Continue</Text>
        <Icon next name='forward-arrow'/>
      </ValidatingButton>
    </Container>;
  }
}

const styles = {
  image: {
    aspectRatio: 1.2,
    borderRadius: 150,
    width: 100
  },
  imageButton: {
    height: 80,
    marginTop: shotgun.contentPadding,
    marginLeft: shotgun.contentPadding,
    marginRight: shotgun.contentPadding,
    width: 'auto',
    borderBottomWidth: 1,
    borderRadius: 2,
    alignItems: 'center'
  },
  imageButtonText: {
    fontSize: 14,
    fontWeight: 'normal'
  },
  imageButtonIconRow: {
    justifyContent: 'center',
    alignItems: 'flex-start'
  }
};

const validationSchema = {
  firstName: yup.string().required().max(30),
  lastName: yup.string().required().max(30),
  password: yup.string().required().max(30),
  email: yup.string().required().email().max(100), //BREAKS in IOS .matches(/^(((\+44\s?\d{4}|\(?0\d{4}\)?)\s?\d{3}\s?\d{3})|((\+44\s?\d{3}|0\d{3})\s?\d{3}\s?\d{4})|((\+44\s?\d{2}|0\d{2})\s?\d{4}\s?\d{4}))?$/).max(35),
  contactNo: yup.string().required().max(35),
};

const drivervalidationSchema = {
  ...validationSchema,
  // CANT ADD IMAGE IN IOS simulator. Should this be mandatory ???? imageData: yup.string().required(),
  dob: yup.date().required()
};

export default withExternalState()(UserDetails);
