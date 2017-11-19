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
    const _this = this;
    const scopedAction = action.bind(_this);
    return observable.subscribe(ev => tryCatch(scopedAction, ev));
};

export default SubscribeWithSensibleErrorHandling;
