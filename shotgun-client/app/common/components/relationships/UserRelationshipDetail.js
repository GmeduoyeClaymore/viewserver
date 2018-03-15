import React, {Component} from 'react';
import { View, Text, Image, TouchableHighlight, Dimensions} from 'react-native';
import Swiper from 'react-native-swiper';
import ReactNativeModal from 'react-native-modal';
import {Spinner, Row, Button} from 'native-base';
import {PagingListView, Icon, ErrorRegion} from 'common/components';
import shotgun from 'native-base-theme/variables/shotgun';
import {callUser} from 'common/actions/CommonActions';
import RNImmediatePhoneCall from 'react-native-immediate-phone-call';
import {connect} from 'custom-redux';
import {getOperationError} from 'common/dao';
const {height, width} = Dimensions.get('window');
const BACKGROUND_COLOR = 'white';
const BORDER_RADIUS = 13;
const BUTTON_FONT_COLOR = '#007ff9';
const BUTTON_FONT_SIZE = 20;
const styles = {
  userSelector: {
    backgroundColor: BACKGROUND_COLOR,
    borderRadius: BORDER_RADIUS,
    width: width - 40,
    height: height - 100,
    margin: 0,
    overflow: 'hidden',
    marginBottom: 10
  },
  container: {
    flex: 1,
    flexDirection: 'row',
    padding: 5,
    paddingTop: 10
  },
  picture: {
    width: 60,
    height: 60,
  },
  view: {
    alignItems: 'flex-start',
    justifyContent: 'flex-start',
    flexDirection: 'row',
    marginTop: 5
  },
  title: {
    fontWeight: 'bold',
    color: '#848484',
    fontSize: 12
  },
  cancelButton: {
    backgroundColor: BACKGROUND_COLOR,
    borderRadius: BORDER_RADIUS,
    height: 57,
    justifyContent: 'center',
  },
  cancelText: {
    padding: 10,
    textAlign: 'center',
    color: BUTTON_FONT_COLOR,
    fontSize: BUTTON_FONT_SIZE,
    fontWeight: '600',
    backgroundColor: 'transparent',
  },
  star: {
    fontSize: 15,
    padding: 2,
    color: shotgun.gold,
  },
  starFilled: {
    fontSize: 15,
    padding: 2,
    color: shotgun.gold,
  },
  starEmpty: {
    fontSize: 15,
    padding: 2,
    color: shotgun.brandLight
  },
  summary: {
    fontSize: 10,
    paddingTop: 5,
    paddingBottom: 5
  }
};

const cancelButton = <Text style={[styles.cancelText]}>Cancel</Text>;
const Paging = () => <View><Spinner /></View>;
const NoItems = () => <View><Text>No items to display</Text></View>;
const starsControl = (rating) => <View style={styles.view}>{[...Array(Math.round(rating))].map((e, i) => <Icon name='star' key={i} style={styles.starFilled}/>)}{[...Array(5 - Math.round(rating))].map((e, i) => <Icon name='star' key={i} style={styles.starEmpty}/>)}</View>;
const jobSummary = ({item: order}) => <View key={order.orderId} style={{flexDirection: 'row', backgroundColor: 'white'}}>
  {order.orderItem ? <Image resizeMode="contain" source={{url: order.orderItem.imageUrl}}  style={styles.picture}/> : null}
  <View style={{flex: 1, padding: 5}}>
    <Text style={styles.title}>{order.product.name }</Text>
    {starsControl(order.customerRating)}
  </View>
</View>;


class UserRelationshipDetail extends Component{
  constructor(props){
    super(props);
    this.handleCancel = this.handleCancel.bind(this);
    this.RelatedUser = this.RelatedUser.bind(this);
    this.selectUserByIndex = this.selectUserByIndex.bind(this);
  }

  RelatedUser = ({user}) => {
    const {onPressCallUser, errors} = this.props;
    return <View style={{flex: 1, width: width - 55, paddingLeft: 10, paddingTop: 10}}>
      <View style={{flexDirection: 'row'}}>
        <Image resizeMode="contain" source={{url: user.imageUrl}}  style={styles.picture}/>
        <View style={{flex: 1, padding: 5}}>
          <Text style={styles.title}>{user.firstName + ' ' + user.lastName}</Text>
          <Text style={styles.summary}>{'ONLINE: ' + user.online}</Text>
          <Text style={styles.summary}>{'STATUS: ' + user.status}</Text>
          <Text style={styles.summary}>{'STATUS MESSAGE: ' + user.statusMessage}</Text>
          <Text style={styles.summary}>{'DISTANCE: ' + user.distance}</Text>
          <View style={{flexDirection: 'row'}}>
            <Text style={styles.summary}>{'RATING: '}</Text>{starsControl(user.rating)}
          </View>
          <ErrorRegion errors={errors}>
            <Button fullWidth callButton onPress={() => onPressCallUser(user)}>
              <Icon name="phone" paddedIcon/>
              <Text uppercase={false}>Call User</Text>
            </Button>
          </ErrorRegion>
        </View>
      </View>
      <PagingListView
        ref={oc => {this.ordersControl = oc;}}
        daoName='orderSummaryDao'
        dataPath={['orders']}C
        options={{driverId: user.userId, reportId: 'driverOrderSummary'}}
        rowView={jobSummary}
        paginationWaitingView={Paging}
        emptyView={NoItems}
        
      />
    </View>;
  }

  handleCancel(){
    const {context} = this. props;
    context.setState({selectedUser: undefined});
  }

  selectUserByIndex(idx){
    if (this.ordersControl){
      this.ordersControl.wrappedInstance.reset();
    }
    const {relatedUsers, context} = this.props;
    context.setState({selectedUser: relatedUsers[idx]});
  }

  render(){
    const {selectedUser, relatedUsers} = this.props;
    const {RelatedUser} = this;
    return <ReactNativeModal
      isVisible={!!selectedUser}
      backdropOpacity={0.4}>
      <View style={styles.userSelector}>
        <Swiper bounces={false} showsButtons={false} showsPagination={false} loadMinimal={true} onIndexChanged={this.selectUserByIndex} index={relatedUsers.indexOf(selectedUser)} style={styles.wrapper} showsButtons={true}>
          {relatedUsers.map((v, i) => <RelatedUser user={v} key={i}/>)}
        </Swiper>
      </View>
      <TouchableHighlight
        style={styles.cancelButton}
        underlayColor="#ebebeb"
        onPress={this.handleCancel}>
        {cancelButton}
      </TouchableHighlight>
    </ReactNativeModal>;
  }
}

const mapStateToProps = (state, initialProps) => {
  const {dispatch} = initialProps;
  const onPressCallUser = async (user) => {
    const {userId, relationshipStatus} = user;
    if (relationshipStatus === 'ACCEPTED'){
      RNImmediatePhoneCall.immediatePhoneCall(`+${user.contactNo}`);
    } else {
      dispatch(callUser({userId}));
    }
  };
  return {
    ...initialProps,
    onPressCallUser,
    errors: getOperationError(state, 'userRelationshipDao', 'callUser'),
  };
};

export default connect(
  mapStateToProps
)(UserRelationshipDetail);


