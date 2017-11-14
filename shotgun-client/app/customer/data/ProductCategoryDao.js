import * as constants from '../../redux/ActionConstants';
import DispatchingDataSink from '../../common/dataSinks/DispatchingDataSink';
import ReportSubscriptionStrategy from '../../common/subscriptionStrategies/ReportSubscriptionStrategy';
import Logger from '../../viewserver-client/Logger';

export default class ProductCategoryDao extends DispatchingDataSink {
  static OPTIONS = {
    offset: 0,
    limit: 10,
    filterMode: 2,
    columnsToSort: [{name: 'category', direction: 'asc'}]
  };

  constructor(client, dispatch, parentCategoryId, options) {
    super();
    this.dispatch = dispatch;
    this.options = Object.assign({}, ProductCategoryDao.OPTIONS, options);
    this.subscriptionStrategy = new ReportSubscriptionStrategy(client, this.getReportContext(parentCategoryId));
  }

  getReportContext(parentCategoryId){
    return {
      reportId: 'productCategory',
      parameters: {
        parentCategoryId
      }
    };
  }

  subscribe(){
    this.dispatch({type: constants.UPDATE_PRODUCT,  product: {status: {busy: true}}});
    this.subscriptionStrategy.subscribe(this, this.options);
  }

  updateOptions(options){
    this.options = Object.assign({}, ProductCategoryDao.OPTIONS, options);
    this.subscriptionStrategy.update(this, this.options);
  }

  onSnapshotComplete(){
    super.onSnapshotComplete();
    this.dispatch({type: constants.UPDATE_PRODUCT, product: {status: {busy: false}}});
  }

  dispatchUpdate(){
    this.dispatch({type: constants.UPDATE_PRODUCT, product: {categories: this.rows}});
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
