import {registerAndLoginPartner} from 'partner/actions/PartnerActions';
import * as ContentTypes from 'common/constants/ContentTypes';
/*eslint-disable */
export  async function nextAction(){
  const {user, bankAccount, address, dispatch, history, next} = this.props;
  let {selectedContentTypes} = this.props;
  if (next){
    history.push(next);
  } else {
    let vehicle = {};
    if (selectedContentTypes[ContentTypes.DELIVERY]){
      vehicle = selectedContentTypes[ContentTypes.DELIVERY].vehicle;
      selectedContentTypes = selectedContentTypes.setIn([ContentTypes.DELIVERY], selectedContentTypes[ContentTypes.DELIVERY].without(['vehicle']));
    }
    const persistedUser = user.setIn(['selectedContentTypes'], selectedContentTypes);
    dispatch(registerAndLoginPartner(persistedUser, vehicle, address, bankAccount, () => history.push('/Root')));
  }
}
/*eslint-enable */
