import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {resolveContentTypeIconSml} from 'common/assets';
import {Icon} from 'common/components';
import {resolveDetailsControl, resolveDetailsClass} from './ContentTypeDetailRegistry';
import {Image, View } from 'react-native';
import {Button, Text} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import ReactNativeModal from 'react-native-modal';
import * as ContentTypes from 'common/constants/ContentTypes';

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('PageTitle', ({contentType}) => contentType.name).
    delivery(() => 'What vehicle have you got?').
    personell(() => 'What skills do you have?').
    rubbish(() => 'Can you do commercial and household waste?');
/*eslint-enable */

export default class ContentTypeSelector extends Component{
  constructor(props){
    super(props);
    this.state = {
      detailVisible: false,
      canSubmit: false,
      showErrors: false,
      content: {}
    };
    this.handleSelectContentType = this.handleSelectContentType.bind(this);
    this.handleConfirm = this.handleConfirm.bind(this);
    this.handleCancel = this.handleCancel.bind(this);
    this.doValidate = this.doValidate.bind(this);
    ContentTypes.resolveResourceFromProps(this.props, resourceDictionary, this);
  }

  handleSelectContentType(){
    const {selected} = this.props;
    if (!selected){
      this.handleToggleDetailVisibility(true);
    } else {
      this.deselectContentType();
    }
  }

  deselectContentType(){
    const {contentType} = this.props;
    let {selectedContentTypes = {}} = this.props;
    const {context} = this.props;
    const content = selectedContentTypes[contentType.contentTypeId];
    if (content){
      selectedContentTypes = {...selectedContentTypes};
      selectedContentTypes[contentType.contentTypeId] = undefined;
      context.setState({selectedContentTypes});
    }
  }

  componentWillReceiveProps(nextProps) {
    ContentTypes.resolveResourceFromProps(nextProps, resourceDictionary, this);
  }

  handleToggleDetailVisibility(detailVisible){
    super.setState({detailVisible});
  }

  handleCancel(){
    this.handleToggleDetailVisibility(false);
  }

  async handleConfirm(){
    const {state} = this;
    const {context} = this.props;
    const result = await this.getValidationResult();
    if (!result || result.error == ''){
      if (context){
        let {selectedContentTypes = {}} = this.props;
        const {contentType, context} = this.props;
        selectedContentTypes = {...selectedContentTypes};
        let selectedProductCategories = state.content.selectedProductCategories || [];
        if (!selectedProductCategories.find(c=> c.categoryId === contentType.productCategory.categoryId)){
          selectedProductCategories = [...selectedProductCategories, contentType.productCategory];
        }
        selectedContentTypes[contentType.contentTypeId] = {...state.content, selectedProductCategories};
        context.setState({selectedContentTypes});
        this.handleToggleDetailVisibility(false);
      }
    } else {
      context.setState({showErrors: true});
    }
  }

  async getValidationResult(){
    const {contentType = {}} = this.props;
    const ContentTypeDetailControl = resolveDetailsClass(contentType);
    if (ContentTypeDetailControl.canSubmit ){
      return await ContentTypeDetailControl.canSubmit(this.state.content);
    }
    return undefined;
  }

  async doValidate(){
    const result = await this.getValidationResult();
    if (result){
      super.setState({canSubmit: !result || result.error == ''});
    }
  }

  setState(newState){
    const existingContent = this.state.content;
    const newContent = {...existingContent, ...newState};
    super.setState({content: newContent}, this.doValidate);
  }

  render(){
    const {resources} = this;
    const {selected, contentType = {}} = this.props;
    const {detailVisible, canSubmit, selectedContentTypes = {}} = this.state;
    const ContentTypeDetailControl = resolveDetailsControl(contentType);
    const stateForContentType = selectedContentTypes[contentType.contentTypeId] || {};

    return <View>
      <Button style={styles.contentTypeButton} large active={selected} onPress={this.handleSelectContentType}>
        <View>
          <Image resizeMode="contain" source={resolveContentTypeIconSml(contentType)} style={styles.contentTypeIcon}/>
          <Text style={styles.contentTypeButtonText}>{contentType.name}</Text>
        </View>
      </Button>
        
      <ReactNativeModal isVisible={detailVisible} style={styles.modal} backdropOpacity={0.4}>
        <View style={styles.contentTypeSelectorContainer}>
          {resources ?  <Text style={styles.title}>{resources.PageTitle(this.props)}</Text> : null }
          <ContentTypeDetailControl {...{contentType, ...this.props, ...stateForContentType, ...this.state.content, context: this, canSubmit}}/>

          <Button paddedBottom fullWidth iconRight onPress={this.handleConfirm} disabled={!canSubmit}>
            <Text uppercase={false}>Confirm</Text>
            <Icon next name='forward-arrow'/>
          </Button>

          <Button paddedBottom fullWidth cancelButton onPress={this.handleCancel}>
            <Text uppercase={false}>Cancel</Text>
          </Button>
        </View>
      </ReactNativeModal>
    </View>;
  }
}

ContentTypeSelector.propTypes = {
  selected: PropTypes.bool,
  contentType: PropTypes.object
};

const styles = {
  contentTypeButton: {
    height: 'auto'
  },
  contentTypeIcon: {
    width: 70,
    height: 70,
    alignSelf: 'center'
  },
  contentTypeButtonText: {
    fontSize: shotgun.noteFontSize,
    paddingTop: 10,
    fontWeight: 'bold',
    textAlign: 'center'
  },
  contentTypeSelectView: {
    flex: 3,
    justifyContent: 'flex-start',
    paddingTop: 30
  },
  modal: {
    margin: 0
  },
  contentTypeSelectorContainer: {
    backgroundColor: shotgun.brandPrimary,
    borderRadius: 0,
    width: shotgun.deviceWidth,
    height: shotgun.deviceHeight,
    overflow: 'hidden',
  },
  title: {
    padding: 20,
    textAlign: 'center',
    color: shotgun.brandLight
  }
};


