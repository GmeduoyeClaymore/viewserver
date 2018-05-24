import React, {Component} from 'react';
import {withExternalState} from 'custom-redux';
import {Image, Dimensions} from 'react-native';
import {Button, Text, Grid, Row} from 'native-base';
import {Icon, ImageSelector} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';
const { width } = Dimensions.get('window');

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


class OrderPhotoUpload extends Component{
  constructor(props){
    super(props);
    this.onChangeValue = this.onChangeValue.bind(this);
    this.onSelectImage = this.onSelectImage.bind(this);
    this.showPicker = this.showPicker.bind(this);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  onChangeValue(field, value){
    const {orderItem={}} = this.props;
    this.setState({orderItem: {...orderItem, [field]: value}});
  }

  onSelectImage(response){
    this.onChangeValue('imageData', response.data);
    imageIsVertical = response.height > response.width;
  }

  showPicker(){
    const {onSelectImage} = this;
    ImageSelector.show({title: 'Select Image', onSelect: onSelectImage, options: {multiple: true}});
  }

  render(){
    const {order, history} = this.props;
    let imageIsVertical = false;
    const {onSelectImage, showPicker, onChangeValue, resources} = this;
    return (
          order.imageData != undefined ? <Grid onPress={showPicker}>
            <Row style={{justifyContent: 'center'}}>
                <Image source={{uri: `data:image/jpeg;base64,${order.imageData}`}} resizeMode='contain' style={[styles.image, {width: imageIsVertical ? width / 2 : width - 50 }]}/>
                <Button style={styles.imageButton} photoButton onPress={showPicker}>
                    <Grid>
                        <Row style={styles.imageButtonIconRow}>
                            <Icon name='camera'/>
                        </Row>
                        <Row style={{justifyContent: 'center'}}>
                            <Text uppercase={false}>{resources.PageCaption}</Text>
                        </Row>
                    </Grid>
                </Button>
            </Row>
          </Grid>: null
    );
  }
}

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
    height: 45
  },
  imageButtonIconRow: {
    justifyContent: 'center',
    alignItems: 'flex-end'
  }
};

export default withExternalState()(OrderPhotoUpload);
