
import Logger from 'common/Logger';

  export const page = dao => ({offset, limit}) => {
    const {rows, totalRowCount} = dao.dataSink;

    if (rows.length >= totalRowCount){
      Logger.info('Reached end of viewport');
      return Promise.resolve();
    }

    if (!dao.options){
      return Promise.reject('No options found in DAO must subscribe before we attempt to page');
    }
    Logger.info(`Paging to offset: ${offset}, limit: ${limit}`);
    const newOptions = {...dao.options, offset, limit};
    return dao.updateSubscription(newOptions);
  };
