import * as constants from '../../redux/ActionConstants';
import DispatchingDataSink from '../../common/dataSinks/DispatchingDataSink';
import DataSourceSubscriptionStrategy from '../../common/subscriptionStrategies/DataSourceSubscriptionStrategy';
import Logger from '../../viewserver-client/Logger';

export default class ProductDao extends DispatchingDataSink {
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    columnsToSort: [{name: 'name', direction: 'asc'}]
  };

  constructor(client, dispatch, options) {
    super();
    this.dispatch = dispatch;
    this.options = Object.assign({}, ProductDao.OPTIONS, options);
    this.subscriptionStrategy = new DataSourceSubscriptionStrategy(client, 'product');
  }

  subscribe(){
    this.dispatch({type: constants.UPDATE_PRODUCT,  product: {status: {busy: true}}});
    this.subscriptionStrategy.subscribe(this, this.options);
  }

  updateOptions(options){
    this.options = Object.assign({}, ProductDao.DEFAULT_OPTIONS, options);
    this.subscriptionStrategy.update(this, this.options);
  }

  onSnapshotComplete(){
    super.onSnapshotComplete();
    this.dispatch({type: constants.UPDATE_PRODUCT, product: {status: {busy: false}}});
  }

  dispatchUpdate(){
    this.dispatch({type: constants.UPDATE_PRODUCT, product: {products: this.rows}});
  }

  page(offset, limit){
    if (this.rows.length >= this.totalRowCount){
      Logger.info('Reached end of viewport');
      return false;
    }

    Logger.info(`Paging: offset ${offset} limit ${limit}`);
    this.updateOptions({offset, limit});
    this.dispatch({type: constants.UPDATE_PRODUCT, product: {status: {busy: true}}});
    return true;
  }
}
