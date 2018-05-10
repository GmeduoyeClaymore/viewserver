import React, {Component} from 'react';
import { View, Text,  Dimensions, TouchableOpacity, ScrollView} from 'react-native';
import ReactNativeModal from 'react-native-modal';
import {Icon, AverageRating} from 'common/components';
import { Row, Grid, Col, ListItem, Button} from 'native-base';
import {callUser} from 'common/actions/CommonActions';
import moment from 'moment';
import {RelatedUser, StatusButton} from './RelatedUser';
import GestureRecognizer from 'react-native-swipe-gestures';
import PhoneCallService from 'common/services/PhoneCallService';
import {connect} from 'custom-redux';

const {height} = Dimensions.get('window');
const ELEMENT_HEIGHT = height - 40;


const NoItems = () => <View style={{flex: 1}}><Text>No items to display</Text></View>;

const JobSummary = ({rating, isLast, isFirst}) => {
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

export class UserDetail extends Component{
  constructor(props){
    super(props);
    this.navigateTo = this.navigateTo.bind(this);
  }
  navigateTo(user, isReverse){
    const {navigateToUser} = this.props;
    navigateToUser(user, isReverse);
  }
  
    renderNextButton = () => {
      const {next} = this.props;
      if (!next){
        return;
      }
      return <TouchableOpacity onPress={() => this.navigateTo(next)} disabled={!next}>
        <View  style={{padding: 5}}>
          <Text style={styles.buttonText}>›</Text>
        </View>
      </TouchableOpacity>;
    }

    renderPrevButton = () => {
      const {prev} = this.props;
      if (!prev){
        return;
      }
      return <TouchableOpacity onPress={() => this.navigateTo(prev, true)} disabled={!prev}>
        <View style={{padding: 5}}>
          <Text style={styles.buttonText}>‹</Text>
        </View>
      </TouchableOpacity>;
    }

    renderButtons = () => {
      const {width, height} = this.props;
      const {renderPrevButton: PrevButton, renderNextButton: NextButton } = this;
      return (
        <View pointerEvents='box-none' style={[styles.buttonWrapper, {width, height}]}>
          <PrevButton/>
          <NextButton/>
        </View>
      );
    }
    onPressCallUser = async (user) => {
      const {dispatch} = this.props;
      const {userId, relationshipStatus} = user;
      if (relationshipStatus === 'ACCEPTED'){
        PhoneCallService.call(user.contactNo);
      } else {
        dispatch(callUser(userId));
      }
    };
      
    render(){
      const {user, onPressCallUser, onPressAssignUser, errors, handleCancel, prev, next} = this.props;
      const {renderButtons: DirectionButtons} = this;
      return [<GestureRecognizer key="1"
        onSwipeLeft={() => this.navigateTo(prev, true)}
        onSwipeRight={() => this.navigateTo(next)}
        config={config}
        style={{flex: 1, margin: 20, flexDirection: 'column', maxHeight: ELEMENT_HEIGHT - 120}}>
        {
          onPressAssignUser ? <Button style={{marginBottom: 15, minHeight: 40, justifyContent: 'flex-start'}} fullWidth statusButton onPress={() => {
            onPressAssignUser(user);
            handleCancel();
          }}>
            <Icon name="dashed" paddedIcon/>
            <Text uppercase={false}>Assign Job To User</Text>
          </Button> : null
        }
        <RelatedUser {...{user, onPressCallUser, onPressAssignUser, errors, handleCancel}} style={{maxHeight: 120, minHeight: 120}}/>
        <View  style={{flex: 4, paddingLeft: 10, paddingRight: 10}}>
          {user.ratings && user.ratings.length ? <ScrollView key='scrollView' style={{flex: 1}} onScroll={this.onScroll}>
            {user.ratings.map((rt, idx) => <JobSummary key={idx} {...this.props} isLast={idx == user.ratings.length - 1} isFirst={idx == 0} rating={rt}/>)}
          </ScrollView> : <NoItems/>}
        </View>
        <StatusButton user={user} style={{justifyContent: 'flex-start', marginLeft: 10, marginRight: 10}}/>
      </GestureRecognizer>, <DirectionButtons  key="2"/>];
    }
}


const findUserFromDao = (state, userId, daoName) => {
  const users = getDaoState(state, ['users'], daoName) || [];
  return  users.find(o => o.userId == userId);
};
  
const mapStateToProps = (state, initialProps) => {
  const userId = getNavigationProps(initialProps).userId;
  if (userId === null){
    throw new Error('Must specify an order id to navigate to this page');
  }
  const daoState = getDao(state, 'singleUserDao');
  if (!daoState){
    return null;
  }
  const user = findUserFromDao(state, userId, 'singleUserDao');
  const errors = getAnyOperationError(state, 'singleUserDao');
  const isPendingUserSubscription = isAnyOperationPending(state, [{ singleUserDao: 'resetSubscription'}]);
  return {
    ...initialProps,
    user,
    userId,
    isPendingUserSubscription,
    errors,
    busyUpdating: isAnyOperationPending(state, ['userRelationshipDao'])
  };
};

const UserWrapper = (props) => {
  const {history} = props;
  return (
    <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='cross' onPress={() => history.goBack()}/>
          </Button>
        </Left>
      </Header>
      <Content keyboardShouldPersistTaps="always">
        <UserDetail {...props}/>
      </Content>
    </Container>
  );
};

const styles = {
  title: {
    fontSize: 20,
  },
  router: {
    paddingLeft: 10,
    paddingRight: 10
  },
  searchContainer: {
    backgroundColor: shotgun.brandPrimary,
    paddingLeft: shotgun.contentPadding,
  }
};


export default connect(mapStateToProps)(UserWrapper);

const config = {
  velocityThreshold: 0.01,
  directionalOffsetThreshold: 100
};
  
