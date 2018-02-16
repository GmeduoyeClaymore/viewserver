import React from 'react';
import {connect} from 'custom-redux';
import {Image, Dimensions} from 'react-native';
import {Button, Container, Content, Header, Text, Title, Body, Left, Grid, Row} from 'native-base';
import yup from 'yup';
import { withRouter } from 'react-router';
import {ValidatingInput, ValidatingButton, Icon, ImageSelector} from 'common/components';

const ItemDetails = ({context, navigationStrategy}) => {
  const {orderItem} = context.state;
  const { width } = Dimensions.get('window');
  let imageIsVertical = false;

  const onChangeValue = (field, value) => {
    const {orderItem={}} = context.state;
    context.setState({orderItem: {...orderItem, [field]: value}});
  };

  const onSelectImage = (response) => {
    onChangeValue('imageData', response.data);
    imageIsVertical = response.height > response.width;
  };

  const showPicker = () => {
    ImageSelector.show({title: 'Select Image', onSelect: onSelectImage, options: {}});
  };

  return (
    <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='back-arrow' onPress={() => navigationStrategy.prev()} />
          </Button>
        </Left>
        <Body><Title>Your Item</Title></Body>
      </Header>
      <Content padded>
        {orderItem.imageData != undefined ? <Grid onPress={showPicker}>
          <Row style={{justifyContent: 'center'}}>
            <Image source={{uri: `data:image/jpeg;base64,${orderItem.imageData}`}} resizeMode='contain' style={[styles.image, {width: imageIsVertical ? width / 2 : width - 50 }]}/>
          </Row>
        </Grid> : null}
        {orderItem.imageData == undefined ? <Button style={styles.imageButton} photoButton onPress={showPicker}>
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
      </Content>
      <ValidatingButton fullWidth iconRight paddedBottom onPress={() =>  navigationStrategy.next()} validationSchema={yup.object(validationSchema)} validateOnMount={true} model={orderItem}>
        <Text uppercase={false}>Continue</Text>
        <Icon next name='forward-arrow'/>
      </ValidatingButton>
    </Container>
  );
};

const validationSchema = {
  notes: yup.string().required().max(200)
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
