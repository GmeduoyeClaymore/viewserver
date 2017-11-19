import Logger from 'common/Logger';

const tryCatch = (action, ev) => {
    try {
        action(ev);
    } catch (error) {
        Logger.error(error);
        throw error;
    }
};

export const SubscribeWithSensibleErrorHandling = (observable, action) => {
    return observable.subscribe(action);
};

export default SubscribeWithSensibleErrorHandling;
