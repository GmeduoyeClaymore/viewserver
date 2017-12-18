import React from 'react';
import {connect} from 'react-redux';
import {Image} from 'react-native';
import {Icon, Button, Container, Form, Input, Header, Text, Title, Body, Left} from 'native-base';
import {merge} from 'lodash';
import { withRouter } from 'react-router';
import ImagePicker from 'react-native-image-picker';

const ItemDetails = ({context, history}) => {
  const {orderItem} = context.state;

  const onChangeValue = (field, value) => {
    context.setState({orderItem: merge({}, orderItem, {[field]: value})});
  };

  const launchImagePicker = () => {
    ImagePicker.showImagePicker(imagePickerOptions, (response) => {
      //TODO - maybe compress the image here??
      onChangeValue('imageData', response.data);
    });
  };


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
        <Input style={styles.detailsInput} value={orderItem.notes} multiline={true} placeholder='Add a description of the item' onChangeText={(value) => onChangeValue('notes', value)}/>
        <Image source={{uri: `data:image/jpeg;base64,${orderItem.imageData}`}} style={styles.image}/>
        <Button onPress={launchImagePicker}><Text>Select Image</Text></Button>
        <Button onPress={() =>  history.push('/Customer/Checkout/OrderConfirmation')}><Text>Continue</Text></Button>
      </Form>
    </Container>
  );
};

const imagePickerOptions = {
  title: 'Item Image',
  storageOptions: {
    skipBackup: true,
    path: 'images'
  }
};

const styles = {
  detailsInput: {
    flex: 1,
    height: 200
  },
  image: {
    flex: 1,
    height: 200
  }
};

export default withRouter(connect()(ItemDetails));
