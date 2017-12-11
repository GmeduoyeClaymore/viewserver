import Logger from 'common/Logger'
import {DAO_REGISTRATION_CONTEXT} from 'custom-redux/DaoMiddleware'

export const GetConnectedClientFromLoginDao = async daoContext => {
    const registrationContext = DAO_REGISTRATION_CONTEXT;
    if(!registrationContext){
        throw new Error("no registration context set ensure you have registered the Dao middleware")
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