import React from 'react';
import {connect} from 'react-redux';
import {Image, Dimensions} from 'react-native';
import {Icon, Button, Container, Content, Header, Text, Title, Body, Left, Grid, Row} from 'native-base';
import yup from 'yup';
import {merge} from 'lodash';
import { withRouter } from 'react-router';
import ImagePicker from 'react-native-image-picker';
import ValidatingInput from 'common/components/ValidatingInput';
import ValidatingButton from 'common/components/ValidatingButton';


const ItemDetails = ({context, history}) => {
  const {orderItem} = context.state;
  const { width } = Dimensions.get('window');
  let imageIsVertical = false;

  const onChangeValue = (field, value) => {
    const {orderItem} = context.state;
    context.setState({orderItem: merge({}, orderItem, {[field]: value})});
  };

  const launchImagePicker = () => {
    ImagePicker.showImagePicker(imagePickerOptions, async (response) => {
      //TODO - maybe compress the image here??
      onChangeValue('imageData', response.data);
      imageIsVertical = response.height > response.width;
    });
  };


  return (
    <Container>
      <Header>
        <Left>
          <Button>
            <Icon name='arrow-back' onPress={() => history.goBack()} />
          </Button>
        </Left>
        <Body><Title>Your Item</Title></Body>
      </Header>
      <Content padded>
        {orderItem.imageData != undefined ? <Grid onPress={launchImagePicker}>
          <Row style={{justifyContent: 'center'}}>
            <Image source={{uri: `data:image/jpeg;base64,${orderItem.imageData}`}} resizeMode='contain' style={[styles.image, {width: imageIsVertical ? width / 2 : width - 50 }]}/>
          </Row>
        </Grid> : null}
        {orderItem.imageData == undefined ? <Button style={styles.imageButton} photoButton onPress={launchImagePicker}>
          <Grid>
            <Row style={styles.imageButtonIconRow}>
              <Icon name='camera' style={{marginBottom: 15}}/>
            </Row>
            <Row style={{justifyContent: 'center'}}>
              <Text uppercase={false}>Add a photo of your item</Text>
            </Row>
          </Grid>
        </Button> : null}
        <ValidatingInput style={styles.detailsInput} value={orderItem.notes} multiline={true} placeholder='Add a description of the item' onChangeText={(value) => onChangeValue('notes', value)} validateOnMount={true} validationSchema={validationSchema.notes} maxLength={200}/>
        <ValidatingButton fullWidth iconRight onPress={() =>  history.push('/Customer/Checkout/OrderConfirmation')} validationSchema={yup.object(validationSchema)} validateOnMount={true} model={orderItem}>
          <Text uppercase={false}>Continue</Text>
          <Icon name='arrow-forward'/>
        </ValidatingButton>
      </Content>
    </Container>
  );
};

const validationSchema = {
  notes: yup.string().required().max(200)
};


const imagePickerOptions = {
  title: 'Item Image',
  mediaType: 'photo',
  storageOptions: {
    skipBackup: true,
    path: 'images'
  }
};

const styles = {
  detailsInput: {
    flex: 1,
    height: 120,
    marginTop: 15,
    textAlignVertical: 'top'
  },
  image: {
    aspectRatio: 1.2,
    borderRadius: 4
  },
  imageButton: {
    height: 150
  },
  imageButtonIconRow: {
    justifyContent: 'center',
    alignItems: 'flex-end'
  }
};

export default withRouter(connect()(ItemDetails));
