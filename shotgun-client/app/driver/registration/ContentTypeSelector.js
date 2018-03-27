import React, {Component} from 'react';
import {resolveContentTypeIconSml} from 'common/assets';
import {Icon} from 'common/components';
import {resolveDetailsControl} from './ContentTypeDetailRegistry';
import {Image, View } from 'react-native';
import {Button, Text} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import ReactNativeModal from 'react-native-modal';
import * as ContentTypes from 'common/constants/ContentTypes';
import {withExternalState} from 'custom-redux';

const resourceDictionary = new ContentTypes.ResourceDictionary();
/*eslint-disable */
resourceDictionary.
  property('PageTitle', ({contentType}) => contentType.name).
    delivery(() => 'What vehicle have you got?').
    personell(() => 'What skills do you have?').
    rubbish(() => 'Can you do commercial and household waste?');
/*eslint-enable */

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
    const {unsavedSelectedContentTypes = {}, contentType} = this.props;
    const content = unsavedSelectedContentTypes[contentType.contentTypeId];
    if (content){
      delete unsavedSelectedContentTypes[contentType.contentTypeId];
      this.setState({unsavedSelectedContentTypes});
    }
  }

  componentWillReceiveProps(nextProps) {
    ContentTypes.resolveResourceFromProps(nextProps, resourceDictionary, this);
    this.doValidate();
  }

  handleToggleDetailVisibility(detailVisible){
    super.setState({detailVisible});
  }

  handleCancel(){
    this.handleToggleDetailVisibility(false);
  }

  async handleConfirm(){
    const {contentType, selectedContentTypes = {}, unsavedSelectedContentTypes = []} = this.props;
    const result = await this.getValidationResult();
    if (!result || result.error == ''){
      const selectedProductCategories = unsavedSelectedContentTypes;
      selectedContentTypes[contentType.contentTypeId] = {...this.state.content, selectedProductCategories};
      this.setState({selectedContentTypes});
      this.handleToggleDetailVisibility(false);
    } else {
      super.setState({showErrors: true});
    }
  }

  async getValidationResult(){
    const {contentType, unsavedSelectedContentTypes = {}} = this.props;
    if (!contentType){
      return undefined;
    }
    const contentForContentType = unsavedSelectedContentTypes[contentType.contentTypeId];
    if (!contentForContentType){
      return undefined;
    }
    const ContentTypeDetailControl = resolveDetailsControl(contentType);
    if (!ContentTypeDetailControl){
      return undefined;
    }
    const canSubmitFunc = ContentTypeDetailControl.validator;
    if (!canSubmitFunc){
      throw new Error('Unable to find canSubmit validation func for component type ' + ContentTypeDetailControl.control.name);
    }
    return await canSubmitFunc(contentForContentType);
  }

  async doValidate(){
    const result = await this.getValidationResult();
    if (result){
      super.setState({canSubmit: !result || result.error == ''});
    }
  }

  render(){
    const {selected, contentType} = this.props;
    const {detailVisible, canSubmit, selectedContentTypes = {}} = this.state;
    const ContentTypeDetailControl = resolveDetailsControl(contentType).control;
    const stateForContentType = selectedContentTypes[contentType.contentTypeId];
    const {stateKey, ...rest} = this.props;

    return <View>
      <Button style={styles.contentTypeButton} large active={selected} onPress={this.handleSelectContentType}>
        <View>
          <Image resizeMode="contain" source={resolveContentTypeIconSml(contentType)} style={styles.contentTypeIcon}/>
          <Text style={styles.contentTypeButtonText}>{contentType.name}</Text>
        </View>
      </Button>
        
      <ReactNativeModal isVisible={detailVisible} style={styles.modal} backdropOpacity={0.4}>
        <View style={styles.contentTypeSelectorContainer}>
          <Text style={styles.title}>{this.resources.PageTitle(this.props)}</Text>
          <ContentTypeDetailControl stateKey={'ContentTypeState' + contentType.contentTypeId} {...rest} stateForContentType={stateForContentType}/>

          <Button padded fullWidth iconRight style={{marginBottom: 5}} onPress={this.handleConfirm} disabled={!canSubmit}>
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

const mapStateToProps = (state, nextOwnProps) => {
  const unsavedSelectedContentTypes = {};
  const {contentTypes = []} = nextOwnProps;
  contentTypes.forEach(
    type => {
      const contentTypeState = state.getIn(['component', 'ContentTypeState' +  type.contentTypeId]);
      if (contentTypeState){
        unsavedSelectedContentTypes[type.contentTypeId] = contentTypeState;
      }
    }
  );

  const {match: parentMatch} = nextOwnProps;
  return {
    parentMatch,
    unsavedSelectedContentTypes,
    ...nextOwnProps
  };
};

export default withExternalState(mapStateToProps)(ContentTypeSelector);

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
    paddingTop: 20,
    textAlign: 'center',
    color: shotgun.brandLight
  }
};


