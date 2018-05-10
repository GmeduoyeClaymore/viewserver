import React, {Component} from 'react';
import {View} from 'react-native';
import ReactNativeModal from 'react-native-modal';
import {UserInfo, Icon, AverageRating} from 'common/components';
import {Text, Row, Grid, Col, ListItem, Button} from 'native-base';
import shotgun from 'native-base-theme/variables/shotgun';
import {updateRelationship} from 'common/actions/CommonActions';
import {withExternalState} from 'custom-redux';
import moment from 'moment';

class UserRelationshipDetail extends Component{
  handleCancel = () => {
    const {history, userRelationshipBasePath} = this.props;
    history.push({pathname: userRelationshipBasePath, transition: 'immediate'});
  }

  onUpdateRelationship = (targetUserId, relationshipStatus, relationshipType) => {
    const {dispatch, user} = this.props;

    dispatch(updateRelationship({targetUserId: user.userId, relationshipStatus, relationshipType}));
  }

  getActionButton = (relationshipStatus, label) => {
    const {busy} = this.props;

    return <Button fullWidth style={styles.statusButton} disabled={busy} onPress={() => this.onUpdateRelationship(relationshipStatus, 'COLLEAGUE')}>
      <Text uppercase={false}>{label}</Text>
    </Button>;
  }

  getStatusButton = () => {
    const {user} = this.props;

    if (user.relationshipStatus === 'ACCEPTED'){
      return this.getActionButton('UNKNOWN', 'Un-Friend');
    }
    if (user.relationshipStatus === 'REQUESTED'){
      if (!user.initiatedByMe){
        return <View  style={style}>{this.getActionButton('ACCEPTED', 'Accept Request')}{this.getActionButton('UNKNOWN', 'Ignore Request')}</View>;
      }
      return this.getActionButton('UNKNOWN', 'Cancel Request');
    }
    if (!user.relationshipStatus || user.relationshipStatus === 'UNKNOWN'){
      return this.getActionButton('REQUESTED', 'Add as friend');
    }
    return 'Unknown ' + user.relationshipStatus;
  }

  render(){
    const {path, history, dispatch, user, onPressAssignUser, handleCancel, selectedUser} = this.props;

    return selectedUser ? <ReactNativeModal isVisible={history.location.pathname.includes(path)} style={styles.modal}>
      {onPressAssignUser ? <Button fullWidth style={styles.actionButton}  onPress={() => {onPressAssignUser(user); handleCancel();}}>
        <Text uppercase={false}>Assign Job To User</Text>
      </Button> : null
      }

      <UserInfo dispatch={dispatch} user={selectedUser} imageWidth={60}/>

      {user.ratings && user.ratings.length ? <ScrollView key='scrollView' style={{flex: 1}} onScroll={this.onScroll}>
        {user.ratings.map(rt => <JobSummary {...this.props} rating={rt}/>)}
      </ScrollView> : <Text style={{alignSelf: 'flex-start'}}>Partner has no completed jobs yet</Text>}

      {this.getStatusButton()}

      <Button fullWidth cancelButton onPress={this.handleCancel}>
        <Text uppercase={false}>Close</Text>
      </Button>
    </ReactNativeModal> : null;
  }
}


const JobSummary = ({rating}) => {
  return <ListItem key={rating.orderId} style={[styles.orderRequest, isLast ? styles.last : undefined, isFirst ?  styles.first : undefined ]}>
    <Grid>
      <Col size={60}>
        <Row style={{width: '100%'}}>
          <Text style={{marginBottom: 8}}>{rating.title }</Text>
        </Row>
        <Row style={{paddingRight: 10, marginBottom: 8}}><Icon paddedIcon name="delivery-time"/><Text style={{paddingTop: 3}}>{moment(rating.updatedDate).format('Do MMM, h:mma')}</Text></Row>
      </Col>
      <Col size={60}>
        <AverageRating rating={rating.rating} text="No Rating"/>
      </Col>
    </Grid>
  </ListItem>;
};

const mapStateToProps = (state, initialProps) => {
  return {
    ...initialProps
  };
};

export default withExternalState(mapStateToProps)(UserRelationshipDetail);

const styles = {
  modal: {
    margin: 0,
    backgroundColor: shotgun.brandPrimary,
    borderRadius: 0,
    width: shotgun.deviceWidth,
    height: shotgun.deviceHeight,
    padding: shotgun.contentPadding
  },
  actionButton: {
    marginBottom: shotgun.contentPadding
  },
  statusButton: {
    marginBottom: 10
  }
};

