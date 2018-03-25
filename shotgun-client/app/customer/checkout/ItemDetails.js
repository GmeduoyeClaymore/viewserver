import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Image, Dimensions} from 'react-native';
import {Button, Container, Content, Header, Text, Title, Body, Left, Grid, Row} from 'native-base';
import yup from 'yup';
import { withRouter } from 'react-router';
import {ValidatingInput, ValidatingButton, Icon, ImageSelector} from 'common/components';
import * as ContentTypes from 'common/constants/ContentTypes';
const { width } = Dimensions.get('window');

/*eslint-disable */
const resourceDictionary = new ContentTypes.ResourceDictionary();
resourceDictionary.
  property('PageTitle', () => 'Photo of Your Item').
    personell(() => 'Photo of Job').
    rubbish(() => 'Photo of Rubbish').
  property('PageCaption', () => 'Add a photo of your item').
    personell('Add a photo of Job').
    rubbish(() => 'Add a photo of your rubbish').
  property('InputPlaceholder', () => 'Add a description of the item').
    personell('Add a description of your job').
    rubbish(() => 'Add a description of your rubbish')
    /*eslint-disable */


class ItemDetails extends Component{
  constructor(props){
    super(props);
    this.onChangeValue = this.onChangeValue.bind(this);
    this.onSelectImage = this.onSelectImage.bind(this);
    this.showPicker = this.showPicker.bind(this);
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
  }

  componentWillReceiveProps(newProps){
    ContentTypes.resolveResourceFromProps(newProps, resourceDictionary, this);
  }

  onChangeValue(field, value){
    const {context} = this.props;
    const {orderItem = {}} = context.state;
    context.setState({orderItem: {...orderItem, [field]: value}});
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
    const {context, navigationStrategy} = this.props;
    const {orderItem} = context.state;
    let imageIsVertical = false;
    const {onSelectImage, showPicker, onChangeValue, resources} = this;
    return (
      <Container>
        <Header withButton>
          <Left>
            <Button onPress={() => navigationStrategy.prev()}>
              <Icon name='back-arrow'/>
            </Button>
          </Left>
          <Body><Title>{resources.PageTitle}</Title></Body>
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
                <Text uppercase={false}>{resources.PageCaption}</Text>
              </Row>
            </Grid>
          </Button> : null}
          <ValidatingInput style={styles.detailsInput} value={orderItem.notes} multiline={true} placeholder={resources.InputPlaceholder} onChangeText={(value) => onChangeValue('notes', value)} validateOnMount={true} validationSchema={validationSchema.notes} maxLength={200}/>
        </Content>
        <ValidatingButton fullWidth iconRight paddedBottom onPress={() =>  navigationStrategy.next()} validationSchema={yup.object(validationSchema)} validateOnMount={true} model={orderItem}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow'/>
        </ValidatingButton>
      </Container>
    );
  
  }
}

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

const mapStateToProps = (state, initialProps) => {

  const {context} = initialProps;
  const {state: contextState} = context;
  const {selectedContentType, selectedProduct} = contextState;
  return {
    ...initialProps,
    selectedContentType
  };
};

export default withRouter(connect(mapStateToProps)(ItemDetails));
