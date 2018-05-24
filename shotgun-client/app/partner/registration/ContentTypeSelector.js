import React, {Component} from 'react';
import {Icon} from 'common/components';
import {resolveDetailsControl} from './ContentTypeDetailRegistry';
import {View} from 'react-native';
import {Button, Text} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import ReactNativeModal from 'react-native-modal';
import * as ContentTypes from 'common/constants/ContentTypes';
import {withExternalState} from 'custom-redux';
import {getDaoState} from 'common/dao';
import {isEqual} from 'lodash';
import Immutable from 'seamless-immutable';

class ContentTypeSelector extends Component{
  constructor(props){
    super(props);
    this.state = {
      detailVisible: false,
      canSubmit: false,
      showErrors: false
    };
    this.handleSelectContentType = this.handleSelectContentType.bind(this);
    this.handleConfirm = this.handleConfirm.bind(this);
    this.handleCancel = this.handleCancel.bind(this);
    this.doValidate = this.doValidate.bind(this);
    ContentTypes.bindToContentTypeResourceDictionary(this, resourceDictionary);
  }

  componentWillReceiveProps(nextProps) {
    if (!isEqual(this.props.unsavedSelectedContentTypes, nextProps.unsavedSelectedContentTypes)){
      this.doValidate(nextProps);
    }
  }

  handleSelectContentType(){
    this.handleToggleDetailVisibility(true);
  }

  deselectContentType(blockPersist){
    const {unsavedSelectedContentTypes = Immutable(this.props.selectedContentTypes || {}), contentType} = this.props;
    const rest = unsavedSelectedContentTypes.without([contentType.contentTypeId]);
    if (blockPersist){
      this.setState({unsavedSelectedContentTypes: rest});
    } else {
      this.setState({unsavedSelectedContentTypes: rest, selectedContentTypes: rest});
    }
  }

  async handleToggleDetailVisibility(detailVisible){
    const {selectedContentTypes} = this.props;
    if (detailVisible){
      await this.doValidate(this.props);
      this.setState({unsavedSelectedContentTypes: selectedContentTypes});
    }
    super.setState({detailVisible});
  }

  handleCancel(){
    this.handleToggleDetailVisibility(false);
  }

  async handleConfirm(){
    const {unsavedSelectedContentTypes = {}, contentType} = this.props;
    const {canSubmit} = this.state;
    const unsavedProductIds = unsavedSelectedContentTypes[contentType.contentTypeId].selectedProductIds;
    const unsavedCategoryIds = unsavedSelectedContentTypes[contentType.contentTypeId].selectedProductCategories;

    if (canSubmit){
      if ((unsavedProductIds && unsavedProductIds.length > 0) || (unsavedCategoryIds && unsavedCategoryIds.length > 0)) {
        this.setState({selectedContentTypes: unsavedSelectedContentTypes});
      } else {
        this.deselectContentType(false);
      }

      await this.handleToggleDetailVisibility(false);
    } else {
      super.setState({showErrors: true});
    }
  }

  async getValidationResult(props){
    const {contentType, unsavedSelectedContentTypes = {}, user} = props;
    if (!contentType){
      return undefined;
    }

    const contentForContentType = unsavedSelectedContentTypes[contentType.contentTypeId];
    if (!contentForContentType){
      return {error: 'must specify some content for content type'};
    }

    const ContentTypeDetailControl = resolveDetailsControl(contentType);
    if (!ContentTypeDetailControl){
      return {error: 'no control found for content type'};
    }
    const canSubmitFunc = ContentTypeDetailControl.validator;
    if (!canSubmitFunc){
      throw new Error('Unable to find canSubmit validation func for component type ' + ContentTypeDetailControl.control.name);
    }
    return await canSubmitFunc(contentForContentType, user);
  }

  async doValidate(props){
    const result = await this.getValidationResult(props);
    super.setState({canSubmit: !result || result.error == ''});
  }

  render(){
    const {selected, contentType} = this.props;
    const {detailVisible, canSubmit, selectedContentTypes = {}} = this.state;
    const ContentTypeDetailControl = resolveDetailsControl(contentType).control;
    const stateForContentType = selectedContentTypes[contentType.contentTypeId];
    const {stateKey, ...rest} = this.props;

    return [
      <View key='button'>
        <Button style={styles.contentTypeButton} large active={selected} onPress={this.handleSelectContentType}>
          <Icon name={`content-type-${contentType.contentTypeId}`}/>
        </Button>
        <Text style={styles.contentTypeButtonText}>{contentType.name}</Text>
      </View>,
      <ReactNativeModal key='modal' isVisible={detailVisible} style={styles.modal}>
        <Text style={styles.title}>{this.resources.PageTitle(this.props)}</Text>
        <ContentTypeDetailControl stateKey={`${stateKey}.unsavedSelectedContentTypes.${contentType.contentTypeId}`} {...rest} stateForContentType={stateForContentType}/>

        <Button fullWidth iconRight style={styles.confirmButton} onPress={this.handleConfirm} disabled={!canSubmit}>
          <Text uppercase={false}>Confirm</Text>
          <Icon next name='forward-arrow'/>
        </Button>

        <Button fullWidth cancelButton onPress={this.handleCancel}>
          <Text uppercase={false}>Cancel</Text>
        </Button>
      </ReactNativeModal>];
  }
}

const styles = {
  contentTypeButton: {
    height: 'auto'
  },
  contentTypeButtonText: {
    fontSize: shotgun.noteFontSize,
    paddingTop: 10,
    fontWeight: 'bold',
    textAlign: 'center'
  },
  modal: {
    margin: 0,
    backgroundColor: shotgun.brandPrimary,
    borderRadius: 0,
    width: shotgun.deviceWidth,
    height: shotgun.deviceHeight,
    padding: shotgun.contentPadding
  },
  title: {
    textAlign: 'center',
    marginBottom: shotgun.contentPadding
  },
  confirmButton: {
    marginBottom: 5
  }
};

const mapStateToProps = (state, nextOwnProps) => {
  const {match: parentMatch} = nextOwnProps;
  return {
    parentMatch,
    ...nextOwnProps,
    user: getDaoState(state, ['user'], 'userDao')
  };
};

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
property('PageTitle', ({contentType}) => contentType.name).
delivery(() => 'What vehicle have you got?').
personell(() => 'What can you do?').
rubbish(() => 'Can you do commercial and household waste?');
/*eslint-enable */

export default withExternalState(mapStateToProps)(ContentTypeSelector);
