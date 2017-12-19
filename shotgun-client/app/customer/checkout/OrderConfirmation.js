import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import {View, Dimensions, Image} from 'react-native';
import {Spinner,  Container, Content, Header, Text, Title, Body, Left, Button, Icon, List, ListItem, H3} from 'native-base';
import {checkout} from 'customer/actions/CustomerActions';
import {isAnyOperationPending, getOperationError, isAnyLoading} from 'common/dao';
import ErrorRegion from 'common/components/ErrorRegion';
import MapViewStatic from 'common/components/maps/MapViewStatic';

const OrderConfirmation = ({client, dispatch, history, errors, busy, orderItem, payment, delivery}) => {
  const purchase = async() => {
    dispatch(checkout(orderItem, payment, delivery, () => history.push('/Customer/Checkout/OrderComplete')));
  };

  const { width } = Dimensions.get('window');
  const {origin, destination} = delivery;

  return <Container>
    <Header>
      <Left>
        <Button transparent>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>Order Summary</Title></Body>
    </Header>
    <Content>
      <List>
        <ListItem>
          <Text>Your guide price is</Text>
          <H3>£XX.XX</H3>
        </ListItem>

        <MapViewStatic client={client} width={width} height={100} origin={origin} destination={destination}/>

        <ListItem>
          <Text>{origin.line1}, {origin.postCode}</Text>
        </ListItem>

        {destination.line1 !== undefined ? <ListItem>
          <Text>{destination.line1}, {destination.postCode}</Text>
        </ListItem> : null}

        <ListItem>
          <H3>Help required?</H3>
          <Text>{delivery.noRequiredForOffload == 0 ? 'No' : `Yes: ${delivery.noRequiredForOffload } People to help`}</Text>
        </ListItem>

        <ListItem>
          <H3>Item Details</H3>
          <Text>{orderItem.notes}</Text>
          {orderItem.imageData !== undefined ?  <Image source={{uri: `data:image/jpeg;base64,${orderItem.imageData}`}} style={styles.image}/> : null}
        </ListItem>
      </List>

      <ErrorRegion errors={errors}><View style={{flex: 1, flexDirection: 'column'}}>
        {!busy ? <Button onPress={purchase}><Text>Place Order</Text></Button> :  <Spinner />}
      </View></ErrorRegion>

    </Content>
  </Container>;
};

const styles = {
  image: {
    flex: 1,
    height: 200
  }
};


OrderConfirmation.PropTypes = {
  orderItem: PropTypes.object,
  delivery: PropTypes.object,
  customer: PropTypes.object
};

const mapStateToProps = (state, initialProps) => {
  const {context} = initialProps;
  const {delivery, payment, orderItem} = context.state;

  return {
    errors: getOperationError(state, 'customerDao', 'checkout'),
    orderItem,
    delivery,
    payment,
    busy: isAnyOperationPending(state, { customerDao: 'checkout'})  || isAnyLoading(state, ['orderItemsDao']),
    ...initialProps
  };
};
export default connect(
  mapStateToProps
)(OrderConfirmation);

