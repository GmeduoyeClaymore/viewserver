import {registerCustomer} from 'customer/actions/CustomerActions';
/*eslint-disable */
export  async function nextAction(){
  const {history, user, deliveryAddress, dispatch, paymentCard, next} = this.props;
  if (next){
    history.push(next);
  } else {
    dispatch(registerCustomer(user, deliveryAddress, paymentCard, () => history.push('/Root')));
  }
}
/*eslint-enable */
