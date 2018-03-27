import React, {Component} from 'react';
import {connect, withExternalState} from 'custom-redux';
import {Button, Container, Header, Text, Title, Body, Left, Grid, Row, Content, View} from 'native-base';
import {getDaoState, isAnyOperationPending, updateSubscriptionAction} from 'common/dao';
import yup from 'yup';
import {LoadingScreen, ValidatingButton, Icon} from 'common/components';

class VehicleSelect extends Component {
  constructor(props) {
    super(props);
  }

  componentDidMount() {
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('productDao', {
      categoryId: '1Vans',
      columnsToSort: [{name: 'productId', direction: 'asc'}]
    }));
  }

  render() {
    const {navigationStrategy, vehicles, busy, orderItem, selectedProduct} = this.props;

    const onSelectVehicle = (selectedProduct) => {
      this.setState({orderItem: {...orderItem, productId: selectedProduct.productId}, selectedProduct});
    };

    return busy ? <LoadingScreen text="Loading Vehicles"/> : <Container>
      <Header withButton>
        <Left>
          <Button onPress={() => navigationStrategy.prev()}>
            <Icon name='back-arrow'/>
          </Button>
        </Left>
        <Body><Title>Select which size van</Title></Body>
      </Header>
      <Content padded>
        <Grid>
          <Row style={{flexWrap: 'wrap'}}>
            {vehicles.map((v, i) => {
              return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
                <Button style={{height: 'auto'}} large active={selectedProduct.productId == v.productId} onPress={() => onSelectVehicle(v)}>
                  <Icon name={v.imageUrl || 'dashed'}/>
                </Button>
                <Text style={styles.vehicleSelectText}>{`${selectedProduct.productId} == ${v.productId}`}</Text>
              </View>;
            })}
          </Row>
        </Grid>
        <Text note style={{marginTop: 10}}>{selectedProduct !== undefined ? selectedProduct.description : null}</Text>
      </Content>
      <ValidatingButton fullWidth paddedBottom iconRight onPress={() => navigationStrategy.next()}
        validateOnMount={true} validationSchema={yup.object(validationSchema)} model={orderItem}>
        <Text uppercase={false}>Continue</Text>
        <Icon next name='forward-arrow'/>
      </ValidatingButton>
    </Container>;
  }
}

const validationSchema = {
  productId: yup.string().required(),
};

const styles = {
  subTitle: {
    marginTop: 25,
    marginBottom: 30,
    fontSize: 13
  },
  vehicleSelectText: {
    width: '100%',
    marginTop: 5,
    marginBottom: 25,
    fontSize: 16,
    textAlign: 'center'
  }
};

//TODO -add error state
const mapStateToProps = (state, initialProps) => {
  const {orderItem, selectedProduct} = initialProps;
  return {
    ...initialProps,
    orderItem,
    selectedProduct,
    busy: isAnyOperationPending(state, [{ productDao: 'updateSubscription'}]),
    vehicles: getDaoState(state, ['product', 'products'], 'productDao')
  };
};

export default withExternalState(mapStateToProps)(VehicleSelect);


