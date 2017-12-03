import Logger from 'common/Logger'

export const GetConnectedClientFromLoginDao = async daoContext => {
    const {registrationContext} = daoContext;
    if(!registrationContext){
        throw new Exception("no registration context set this should happen when you register this dao")
    }
    let {loginDao} = registrationContext.daos;
    if(!loginDao){
      Logger.info("Login dao not found waiting for registration")
      loginDao = await registrationContext.registrationSubject.filter(ev => ev.name === "loginDao").take(1).toPromise();
    }
    let {client} = loginDao;
    if(!client){
        Logger.info("Waiting for login dao to login")
        client = await loginDao.clientsubject.take(1).toPromise();
    }
    if(!client.connected){
        Logger.info("Waiting for client to connect")
    }
    await loginDao.client.connect();

    return loginDao.client;
}