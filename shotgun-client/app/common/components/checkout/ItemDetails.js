import React, {Component} from 'react';
import {withExternalState} from 'custom-redux';
import {Image} from 'react-native';
import {Button, Container, Content, Header, Text, Title, Body, Left, Grid, Row} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import yup from 'yup';
import {ValidatingInput, ValidatingButton, Icon, ImageSelector} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';

class ItemDetails extends Component{
  constructor(props){
    super(props);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  onChangeValue = (field, value) => {
    const {order = {}} = this.props;
    this.setState({order: {...order, [field]: value}});
  }

  onSelectImage = (response) => {
    this.onChangeValue('imageData', response.data);
    imageIsVertical = response.height > response.width;
  }

  showPicker = () => {
    const {onSelectImage} = this;
    ImageSelector.show({title: 'Select Image', onSelect: onSelectImage, options: {}});
  }

  render(){
    const {next, order, history} = this.props;
    const {imageData} = order;
    const imageIsVertical = false;

    return (
      <Container>
        <Header withButton>
          <Left>
            <Button onPress={() => history.goBack()}>
              <Icon name='back-arrow'/>
            </Button>
          </Left>
          <Body><Title>{this.resources.PageTitle}</Title></Body>
        </Header>
        <Content padded>
          <ValidatingInput style={styles.titleInput}  bold placeholder="Enter job title" value={order.title} validateOnMount={order.title !== undefined} onChangeText={(value) => this.onChangeValue('title', value)} validationSchema={validationSchema.title} maxLength={30}/>
          {imageData != undefined ? <Grid onPress={this.showPicker}>
            <Row style={{justifyContent: 'center'}}>
              <Image source={{uri: `data:image/jpeg;base64,${imageData}`}} resizeMode='contain' style={[styles.image, {width: imageIsVertical ? shotgun.deviceWidth / 2 : shotgun.deviceWidth - 50 }]}/>
            </Row>
          </Grid> : null}
          {imageData == undefined ? <Button style={styles.imageButton} photoButton onPress={this.showPicker}>
            <Grid>
              <Row style={styles.imageButtonIconRow}>
                <Icon name='camera' style={{marginBottom: 15}}/>
              </Row>
              <Row style={{justifyContent: 'center'}}>
                <Text uppercase={false}>{this.resources.PageCaption}</Text>
              </Row>
            </Grid>
          </Button> : null}
          <ValidatingInput style={styles.detailsInput} value={order.description} multiline={true} placeholder={this.resources.InputPlaceholder} showIcons={false} onChangeText={(value) => this.onChangeValue('description', value)} validateOnMount={true} validationSchema={validationSchema.description} maxLength={200}/>
          <ValidatingButton fullWidth iconRight arrow={true} onPress={() =>  history.push(next)} validationSchema={yup.object(validationSchema)} validateOnMount={true} model={order}>
            <Text uppercase={false}>Continue</Text>
          </ValidatingButton>
        </Content>
      </Container>
    );
  }
}

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
property('PageTitle', 'Description of Your Item').
personell('Description of the job').
rubbish('Description of the rubbish').
property('PageCaption', 'Add a photo of your item').
personell('Add a photo of the job').
rubbish('Add a photo of your rubbish').
property('InputPlaceholder', 'Add a description of the item').
personell('Add a description of your job').
rubbish('Add a description of your rubbish')
/*eslint-disable */



const validationSchema = {
  title: yup.string().required().max(30),
  description: yup.string().max(200)
};

const styles = {
  detailsInput: {
    height: 120,
    marginTop: 15,
    textAlignVertical: 'top'
  },
  titleInput: {
    height: 45,
    marginTop: 15,
    marginBottom: 15,
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
