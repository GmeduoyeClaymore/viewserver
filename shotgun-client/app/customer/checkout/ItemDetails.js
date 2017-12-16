import React, {Component} from 'react';
import {connect} from 'react-redux';
import {Picker, Slider} from 'react-native';
import {Icon, Button, Container, Form, Input, Item, Header, Text, Title, Body, Left} from 'native-base';
import {getDaoState} from 'common/dao';
import {merge} from 'lodash';
import { withRouter } from 'react-router';
import ImagePicker from 'react-native-image-picker';

const ItemDetails = ({context, history}) => {
  const {order} = context.state;

  const onChangeValue = (field, value) => {
    context.setState({order: merge({}, order, {[field]: value})});
  };

  const launchImagePicker = () => {
    ImagePicker.showImagePicker(imagePickerOptions, (response) => {
      console.log('Response = ', response);

      if (response.didCancel) {
        console.log('User cancelled image picker');
      }
      else if (response.error) {
        console.log('ImagePicker Error: ', response.error);
      }
      else if (response.customButton) {
        console.log('User tapped custom button: ', response.customButton);
      }
      else {
        let source = { uri: response.uri };

        // You can also display the image using data:
        // let source = { uri: 'data:image/jpeg;base64,' + response.data };

        this.setState({
          avatarSource: source
        });
      }
    });
  }


  return (
    <Container>
      <Header>
        <Left>
          <Button transparent>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Item Details</Title></Body>
      </Header>
      <Form style={{display: 'flex', flex: 1}}>
        <Input style={styles.detailsInput} value={order.notes} multiline={true} placeholder='Add a description of the item' onChangeText={(value) => onChangeValue('notes', value)}/>
        <Button onPress={launchImagePicker}><Text>Select Image</Text></Button>
        <Button onPress={() =>  history.push('/Customer/Checkout/OrderConfirmation')}><Text>Continue</Text></Button>
      </Form>
    </Container>
  );
};

const imagePickerOptions = {
  title: 'Select Avatar',
  customButtons: [
    {name: 'fb', title: 'Choose Photo from Facebook'},
  ],
  storageOptions: {
    skipBackup: true,
    path: 'images'
  }
};

const styles = {
  detailsInput: {
    width: 200,
    height: 200
  }
};

export default withRouter(connect()(ItemDetails));
