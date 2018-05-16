import Logger from 'common/Logger';

export default class NotificationActionHandlerService {
  static async handleAction(history, baseUrl, actionUri){
    const parsedOrderActionUri = NotificationActionHandlerService.parseOrderActionUri(actionUri);
    if (parsedOrderActionUri){
      const route = parsedOrderActionUri[1].replace('~', '/');
      const orderId = parsedOrderActionUri[2];

      history.push(`${baseUrl}/${route}`, {orderId});
    } else {
      const parseUserActionUri = NotificationActionHandlerService.parseUserActionUri(actionUri);
      if (parseUserActionUri){
        const route = parseUserActionUri[1].replace('~', '/');
        const userId = parseUserActionUri[2];
        history.push(`${baseUrl}/${route}`, {userId});
      }
    }
  }

  static parseOrderActionUri(actionUri){
    Logger.debug(`Received notification action ${actionUri}`);
    return actionUri ? actionUri.match(/^^shotgun:\/\/([^\/]+)\/?([\w-]+)?$/) : undefined;
  }

  static parseUserActionUri(actionUri){
    Logger.debug(`Received notification action ${actionUri}`);
    return actionUri ? actionUri.match(/^^shotgunu:\/\/([^\/]+)\/?([\w-]+)?$/) : undefined;
  }
}
