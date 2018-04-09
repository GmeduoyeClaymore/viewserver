import Logger from 'common/Logger';

export default class NotificationActionHandlerService {
  static async handleAction(history, baseUrl, actionUri){
    const parsedOrderActionUri = NotificationActionHandlerService.parseOrderActionUri(actionUri);
    if (parsedOrderActionUri){
      const route = parsedOrderActionUri[1];
      const orderId = parsedOrderActionUri[2];

      history.push(`${baseUrl}/${route}`, {orderId});
    } else {
      const parsedRelationshipActionUri = NotificationActionHandlerService.parseRelationshipActionUri(actionUri);
      history.push(`${baseUrl}/UserRelationships/${parsedRelationshipActionUri[1]}`);
    }
  }

  static parseOrderActionUri(actionUri){
    Logger.debug(`Received notification action ${actionUri}`);
    return actionUri.match(/^shotgun:\/\/(\w+)\/?([\w-]+)?$/);
  }

  static parseRelationshipActionUri(actionUri){
    Logger.debug(`Received notification action ${actionUri}`);
    return actionUri.match(/^shotgun:\/\/Landing\/UserRelationships\/(.*)$/);
  }
}
