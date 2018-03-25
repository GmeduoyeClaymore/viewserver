import React, {Component}  from 'react';
import { connect, setStateIfIsMounted } from 'custom-redux';
import { Row, Picker, Input, View} from 'native-base';
import { withRouter } from 'react-router';
import {ErrorRegion, LoadingScreen} from 'common/components';
import { getDaoState, isAnyOperationPending, getDaoSize, getOperationError } from 'common/dao';
import shotgun from 'native-base-theme/variables/shotgun';
import {updateRange, updateStatus} from 'common/actions/CommonActions';

class UserRelationships extends Component{
  constructor(props){
    super(props);
    this.state = {};
    setStateIfIsMounted(this);
    this.updateStatusMessage = this.updateStatusMessage.bind(this);
    this.setStatus = this.setStatus.bind(this);
  }

  componentWillReceiveProps(newProps){
    const {me} = newProps;
    if (me && me.statusMessage  !== this.state.statusMessage){
      this.setState({statusMessage: me.statusMessage});
    }
  }

  setSelectedUser(selectedUser){
    this.setState({selectedUser, selectedTabIndex: 2});
  }

  updateStatusMessage(statusMessage){
    this.setState({statusMessage});
  }

  setStatus(args){
    const {statusMessage, status, setStatus} = this.props;
    const payload = {statusMessage, status, ...args};
    setStatus(payload);
  }

  render(){
    const {state} = this;
    const {errors, me} = this.props;
    if (!me){
      return <LoadingScreen text="Loading.."/>;
    }
    return <ErrorRegion errors={errors}>
      <View style={{ flex: 1, padding: 15}}>
        <Row style={{flex: 1}}>
          <Picker
            itemStyle={{paddingTop: 10, paddingBottom: 10, paddingLeft: 10}}
            iosHeader="Select one"
            mode="dropdown"
            selectedValue={me.status}
            onValueChange={(status) => this.setStatus({status})}>
            <Picker.Item padded color="#008000" label="Online" value="ONLINE" />
            <Picker.Item padded color="#FFA500" label="Busy" value="BUSY" />
            <Picker.Item padded color="#0000ff" label="Available Soon" value="AVAILABLESOON" />
            <Picker.Item padded color="#ff0000" label="Appear Offline" value="OFFLINE" />
          </Picker>
        </Row>
        <Row style={{flex: 1}}>
          <Input value={state.statusMessage} placeholder="What are you up to ?" placeholderTextColor={shotgun.silver} onChangeText={value => this.updateStatusMessage(value)} onBlur={() => this.setStatus({statusMessage: state.statusMessage})}/>
        </Row>
      </View>
    </ErrorRegion>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const {dispatch} = initialProps;
  const setRange = (range) => dispatch(updateRange(range));
  const setStatus = ({status, statusMessage}) => dispatch(updateStatus({status, statusMessage}));

  return {
    ...initialProps,
    setRange,
    setStatus,
    noRelationships: getDaoSize(state, 'userRelationshipDao'),
    errors: getOperationError(state, 'userRelationshipDao', 'updateRelationship'),
    me: getDaoState(state, ['user'], 'userDao'),
    busy: isAnyOperationPending(state, [{userRelationshipDao: 'updateSubscription'}])
  };
};

export default withRouter(connect(
  mapStateToProps
)(UserRelationships));


