import Logger from '../../viewserver-client/Logger';

export default class AppPage{
    constructor(tree){
        this.tree = tree;
    }

    waitForLoaderToClear(timeout = 5000, retries = 10){
        console.log(`Waiting for app loader to clear current state is ${this.isWaitScreenShowing()}`)
        let retryInterval = timeout/retries;
        let _context = this;
        return new Promise((resolve, reject) => {
            const result = (counter) => {
                if(!_context.isWaitScreenShowing()){
                    resolve();
                }
                else if(counter === retries){
                    reject( `Unable to detect loading screen has cleared after ${retries} retries. Total time elapsed ${timeout} ms. Current screen content is ${JSON.stringify(this.tree.toJSON())}`)
                }else{
                    Logger.debug(`Attempt ${counter} Loader not cleared trying again in  ${retryInterval}ms`)
                    setTimeout(() => result(counter+1),
                        retryInterval
                    );
                }
                
            }
            result(0);
        });
    }

    isWaitScreenShowing(){
        const content = this.tree.toJSON()
        if(content.type === 'ExponentAppLoading'){
            return true;
        }
        return false;
    }
}