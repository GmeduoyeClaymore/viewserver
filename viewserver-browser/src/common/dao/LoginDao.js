import Logger from 'common/Logger';
import {Rx} from 'common/rx';
import * as crx from 'common/rx';
import {page} from 'common/dao/DaoExtensions';
import {isEqual} from 'lodash';
import Client from 'viewserver-client/Client';
import ProtoLoader from 'viewserver-client/core/ProtoLoader';
import ValidationService from 'common/utils/ValidationService';
import yup from 'yup';

export const validationSchema = {
    username: yup.string().required().min(3),
    password: yup.string().required().min(3),
    url: yup.string()
      .matches(/^((wss[s]?|ftp):\/)?\/?([^:\/\s]+):((\w+)*)/i,'url')
      .required()
}

export default class LoginDao {
  constructor() {
    this.subject = new Rx.Subject();
    this.clientsubject = new Rx.Subject();
    this.optionsSubject = new Rx.Subject();
    this.name = "loginDao";

    this.login = this.login.bind(this);
  }
    
  get observable(){
    return this.subject;
  }
    
  get optionsObservable(){
    return this.optionsSubject;
  }

  async login(options){
    const result = await ValidationService.validate(options, yup.object(validationSchema));
    if(result.error){
      throw new Error(Object.values(result).join('\n'));
    }
    const {username, password, url} = options;
    const client = new Client(`ws://${url}/`);
    await ProtoLoader.loadAll();
    var connectObservable = Rx.Observable.fromPromise(client.connect());
    await connectObservable.take(1).timeoutWithError(10000,'Unable to connect to server after 10 seconds').toPromise();
    this.client = client;
    this.clientsubject.next(this.client);
    return `logged in as ${JSON.stringify(options)}`;
  }
}

