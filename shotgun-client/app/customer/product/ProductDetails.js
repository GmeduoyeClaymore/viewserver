import React from 'react';
import PropTypes from 'prop-types';
import { View, Image, StyleSheet } from 'react-native';
import {Text, Content, Header, Left, Body, Container, Button, Icon, Title} from 'native-base';
import ProductActionBar from './ProductActionBar';
import ErrorRegion from 'common/components/ErrorRegion';
import {isOperationPending, getOperationError} from 'common/dao';
import {connect} from 'react-redux';

const ProductDetails = ({location, history, dispatch, errors}) => {
  const {state = {}} = location;
  const {product} = state;
  return <Container>
    <Header>
      <Left>
        <Button transparent>
          <Icon name='arrow-back' onPress={() => history.goBack()} />
        </Button>
      </Left>
      <Body><Title>{product.name}</Title></Body>
    </Header>
    <Content>
      <ErrorRegion errors={errors}>
        <View style={styles.container}>
          <Image source={require('../assets/cement.jpg')} style={styles.picture} />
          <Text style={[styles.mediumText, styles.lightText]}>{product.description}</Text>
          <ProductActionBar product={product} dispatch={dispatch}/>
        </View>
      </ErrorRegion>
    </Content>
  </Container>;
};

ProductDetails.PropTypes = {
  product: PropTypes.object
};

const mapStateToProps = (state, nextOwnProps) => ({
  busy: isOperationPending(state, 'cartItemsDao', 'addItemToCart'),
  errors: getOperationError(state, 'cartItemsDao', 'addItemToCart'),
  ...nextOwnProps
});

const ConnectedProductDetails =  connect(mapStateToProps)(ProductDetails);

export default ConnectedProductDetails;

const styles = StyleSheet.create({
  container: {
    marginTop: 5,
    marginLeft: 10,
    marginRight: 10,
    backgroundColor: '#FFFFFF',
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'flex-start',
    alignItems: 'center'
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    height: 30
  },
  picture: {
    width: 80,
    height: 80,
    borderRadius: 40,
    marginBottom: 4,
    marginTop: 10
  },
  mediumText: {
    fontSize: 16,
  },
  bigText: {
    fontSize: 20,
    flex: 5
  },
  separator: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#AAAAAA',
  },
  list: {
    flex: 1,
  },
  emptyList: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  lightText: {
    color: '#C7C7CC'
  }
});
