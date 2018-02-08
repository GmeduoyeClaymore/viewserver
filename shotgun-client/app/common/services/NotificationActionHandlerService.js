import Logger from 'common/Logger';

export default class NotificationActionHandlerService {
  static async handleAction(history, baseUrl, actionUri){
    const parsedActionUri = NotificationActionHandlerService.parseActionUri(actionUri);
    const route = parsedActionUri[1];
    const orderId = parsedActionUri[2];

    history.push(`/${baseUrl}/${route}`, {orderId});
  }

  static parseActionUri(actionUri){
    Logger.debug(`Received notification action ${actionUri}`);
    return actionUri.match(/^shotgun:\/\/(\w+)\/?([\w-]+)?$/);
  }
}
