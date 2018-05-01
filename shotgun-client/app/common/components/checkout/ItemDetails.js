import React, {Component} from 'react';
import {withExternalState} from 'custom-redux';
import {Image} from 'react-native';
import {Button, Container, Content, Header, Text, Title, Body, Left, Grid, Row} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, Icon, ImageSelector} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', 'Photo of Your Item').
    personell('Photo of Job').
    rubbish('Photo of Rubbish').
  property('PageCaption', 'Add a photo of your item').
    personell('Add a photo of Job').
    rubbish('Add a photo of your rubbish').
  property('InputPlaceholder', 'Add a description of the item').
    personell('Add a description of your job').
    rubbish('Add a description of your rubbish')
    /*eslint-disable */


class ItemDetails extends Component{
  constructor(props){
    super(props);
    this.onChangeValue = this.onChangeValue.bind(this);
    this.onSelectImage = this.onSelectImage.bind(this);
    this.showPicker = this.showPicker.bind(this);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  onChangeValue(field, value){
    const {order={}} = this.props;
    this.setState({order: {...order, [field]: value}});
  }

  onSelectImage(response){
    this.onChangeValue('imageData', response.data);
    imageIsVertical = response.height > response.width;
  }

  showPicker(){
    const {onSelectImage} = this;
    ImageSelector.show({title: 'Select Image', onSelect: onSelectImage, options: {}});
  }

  render(){
    const {next, order, history, imageData} = this.props;
    let imageIsVertical = false;
    const {onSelectImage, showPicker, onChangeValue, resources} = this;
    return (
      <Container>
        <Header withButton>
          <Left>
            <Button onPress={() => history.goBack()}>
              <Icon name='back-arrow'/>
            </Button>
          </Left>
          <Body><Title>{resources.PageTitle}</Title></Body>
        </Header>
        <Content padded>
          {imageData != undefined ? <Grid onPress={showPicker}>
            <Row style={{justifyContent: 'center'}}>
              <Image source={{uri: `data:image/jpeg;base64,${imageData}`}} resizeMode='contain' style={[styles.image, {width: imageIsVertical ? shotgun.deviceWidth / 2 : shotgun.deviceWidth - 50 }]}/>
            </Row>
          </Grid> : null}
          {imageData == undefined ? <Button style={styles.imageButton} photoButton onPress={showPicker}>
            <Grid>
              <Row style={styles.imageButtonIconRow}>
                <Icon name='camera' style={{marginBottom: 15}}/>
              </Row>
              <Row style={{justifyContent: 'center'}}>
                <Text uppercase={false}>{resources.PageCaption}</Text>
              </Row>
            </Grid>
          </Button> : null}
          <ValidatingInput style={styles.detailsInput} value={order.description} multiline={true} placeholder={resources.InputPlaceholder} onChangeText={(value) => onChangeValue('description', value)} validateOnMount={true} validationSchema={validationSchema.description} maxLength={200}/>
        </Content>
        <ValidatingButton fullWidth iconRight paddedBottom onPress={() =>  history.push(next)} validationSchema={yup.object(validationSchema)} validateOnMount={true} model={order}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow'/>
        </ValidatingButton>
      </Container>
    );
  
  }
}

const validationSchema = {
  description: yup.string().required().max(200)
};

const styles = {
  detailsInput: {
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

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps,
  };
};

export default withExternalState(mapStateToProps)(ItemDetails);
