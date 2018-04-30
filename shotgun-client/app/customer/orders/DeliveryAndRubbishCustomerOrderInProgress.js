
import React, {Component} from 'react';
import PartnerDetails from './PartnerDetails';
import VehicleDetails from './VehicleDetails';
import CallButtons from './CallButtons';


export default class DeliveryAndRubbishCustomerOrderInProgress extends Component{
  constructor(props) {
    super(props);
  }
  
  render() {
    const {parentPath} = this;
    return <Grid>
      <Row size={60}>
        <Button transparent style={styles.backButton} onPress={() => history.push(`${parentPath}/CustomerOrders`)}>
          <Icon name='back-arrow'/>
        </Button>
      </Row>
      <PartnerDetails  {...this.props}/>
      <VehicleDetails {...this.props}/>
      <CallButtons    {...this.props}/>
    </Grid>;
  }
}

const styles = {
  backButton: {
    position: 'absolute',
    left: 0,
    top: 0
  }
};
