import React, {Component} from 'react';
import {connect} from 'custom-redux';
import {Button, Container, Header, Text, Title, Body, Left, Grid, Row, Content, View} from 'native-base';
import {getDaoState, isAnyOperationPending, updateSubscriptionAction} from 'common/dao';
import {withRouter} from 'react-router';
import yup from 'yup';
import {LoadingScreen, ValidatingButton, Icon} from 'common/components';

class VehicleSelect extends Component {
  constructor(props) {
    super(props);
  }

  componentWillMount() {
    const {dispatch} = this.props;
    dispatch(updateSubscriptionAction('productDao', {
      categoryId: '1Vans',
      columnsToSort: [{name: 'productId', direction: 'asc'}]
    }));
  }

  render() {
    const {navigationStrategy, vehicles, context, busy} = this.props;
    const {orderItem, selectedProduct} = context.state;

    const onSelectVehicle = (selectedProduct) => {
      context.setState({orderItem: {...orderItem, productId: selectedProduct.productId}, selectedProduct});
    };

    return busy ? <LoadingScreen text="Loading Vehicles"/> : <Container>
      <Header withButton>
        <Left>
          <Button>
            <Icon name='back-arrow' onPress={() => navigationStrategy.prev()}/>
          </Button>
        </Left>
        <Body><Title>Select which size van</Title></Body>
      </Header>
      <Content padded>
        <Text style={styles.subTitle}>Select the type of van you think you will need for your order</Text>
        <Grid>
          <Row style={{flexWrap: 'wrap'}}>
            {vehicles.map((v, i) => {
              return <View key={i} style={{width: '50%', paddingRight: i % 2 == 0 ? 10 : 0, paddingLeft: i % 2 == 0 ? 0 : 10}}>
                <Button style={{height: 'auto'}} large active={orderItem.productId == v.productId} onPress={() => onSelectVehicle(v)}>
                  <Icon name={v.imageUrl || 'dashed'}/>
                </Button>
                <Text style={styles.vehicleSelectText}>{v.name}</Text>
              </View>;
            })}
          </Row>
        </Grid>
        <ValidatingButton fullWidth paddedLeftRight iconRight onPress={() => navigationStrategy.next()}
          validateOnMount={true} validationSchema={yup.object(validationSchema)} model={orderItem}>
          <Text uppercase={false}>Continue</Text>
          <Icon next name='forward-arrow'/>
        </ValidatingButton>
        <Text note style={{marginTop: 10}}>{selectedProduct !== undefined ? selectedProduct.description : null}</Text>
      </Content>
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
const mapStateToProps = (state, initialProps) => ({
  ...initialProps,
  busy: isAnyOperationPending(state, [{ productDao: 'updateSubscription'}]),
  vehicles: getDaoState(state, ['product', 'products'], 'productDao')
});

export default withRouter(connect(
  mapStateToProps
)(VehicleSelect));


