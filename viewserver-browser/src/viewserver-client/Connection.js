import Logger from 'common/Logger';
import ProtoLoader from './core/ProtoLoader';

export default class Connection {
  constructor(uri, network, autoReconnect) {
    this._uri = uri;
    this._network = network;
    this._commandId = 1;
    this._openCommands = [];
    this._isOpen = false;
    this._autoReconnect = autoReconnect;
    this._forcedClose = false;
    this.handleSocketOpen = this.handleSocketOpen.bind(this);
    this.handleSocketClosed = this.handleSocketClosed.bind(this);
    this.handleSocketMessage = this.handleSocketMessage.bind(this);
  }

    static CONNECTION_ATTEMPT_DELAY = 500; // The maximum amount of time to wait for the socket to open before giving up
    static CONNECTING_RETRY_ATTEMPTS = 5;

    connect() {
      if(!this.connectionPromise){
        this.connectionPromise = new Promise((resolve, reject) => {
          this.tryConnect(1, resolve, reject);
        });
      }
      return this.connectionPromise;
    }

    tryConnect(attemptCounter, resolve, reject){
      Logger.debug(`Creating web socket`);
      this._socket = new WebSocket(this._uri);
      this._socket.binaryType = 'arraybuffer';

      this._socket.onopen = () => {
        Logger.debug(`Attempt ${attemptCounter} socket connected successfully`);
        this._socket.onclose = this.handleSocketClosed;
        this.handleSocketOpen();
        resolve();
      };

      this._socket.onerror = () => {
        if (attemptCounter === Connection.CONNECTING_RETRY_ATTEMPTS){
          this.connectionPromise  = undefined;
          reject(`Unable to detect open web socket to ${this._uri} after ${Connection.CONNECTING_RETRY_ATTEMPTS} retries`);
        } else {
          Logger.debug(`Attempt ${attemptCounter}/${Connection.CONNECTING_RETRY_ATTEMPTS} socket not connected. Socket state is ${this._socket.readyState} Trying again in ${Connection.CONNECTION_ATTEMPT_DELAY}ms`);
          setTimeout(() => this.tryConnect(attemptCounter + 1, resolve, reject), Connection.CONNECTION_ATTEMPT_DELAY);
        }
      };
      this._socket.onmessage = this.handleSocketMessage;
    }

    get socket(){
      return this._socket;
    }

    get connected(){
      return this._isOpen;
    }
    
    get network(){
      return this._network;
    }
    
    get openCommands(){
      return this._openCommands;
    }
    
    get commandId(){
      return this._commandId;
    }

    handleSocketOpen(){
      Logger.debug('Network Connected');
      this._isOpen = true;
    }

    handleSocketClosed(e){
      let reason;

      switch (e.code){
      case 1000:
        reason = 'Normal closure, meaning that the purpose for which the connection was established has been fulfilled.';
        break;
      case 1001:
        reason = 'An endpoint is \'going away\', such as a server going down or a browser having navigated away from a page.';
        break;
      case 1002:
        reason = 'An endpoint is terminating the connection due to a protocol error';
        break;
      case 1003:
        reason = 'An endpoint is terminating the connection because it has received a type of data it cannot accept (e.g., an endpoint that understands only text data MAY send this if it receives a binary message).';
        break;
      case 1004:
        reason = 'Reserved. The specific meaning might be defined in the future.';
        break;
      case 1005:
        reason = 'No status code was actually present.';
        break;
      case 1006:
        reason = 'The connection was closed abnormally, e.g., without sending or receiving a Close control frame';
        break;
      case 1007:
        reason = 'An endpoint is terminating the connection because it has received data within a message that was not consistent with the type of the message (e.g., non-UTF-8 [http://tools.ietf.org/html/rfc3629] data within a text message).';
        break;
      case 1008:
        reason = 'An endpoint is terminating the connection because it has received a message that \'violates its policy\'. This reason is given either if there is no other sutible reason, or if there is a need to hide specific details about the policy.';
        break;
      case 1009:
        reason = 'An endpoint is terminating the connection because it has received a message that is too big for it to process.';
        break;
      case 1010:
        reason = 'An endpoint (client) is terminating the connection because it has expected the server to negotiate one or more extension, but the server didn\'t return them in the response message of the WebSocket handshake. Specifically, the extensions that are needed are: ' + e.reason;
        break;
      case 1011:
        reason = 'A server is terminating the connection because it encountered an unexpected condition that prevented it from fulfilling the request.';
        break;
      case 1015:
        reason = 'The connection was closed due to a failure to perform a TLS handshake';
        break;
      default:
        reason = 'Unknown reason';
      }

      Logger.debug(`Network Disconnected ${reason}`);

      if (this._autoReconnect == true && !this._forcedClose){
        Logger.debug('Attempting auto reconnect');
        this.connect(this._autoReconnect);
      }

      if (e.code > 1000 && e.wasClean === false){
        Logger.error(`Error ${e.code} \'${reason}\' when closing socket`);
      }
    }

    handleSocketMessage(evt) {
      Logger.fine('Network Received msg');
      if (evt.data.byteLength === 0) {
        return;
      }

  
      const msg = ProtoLoader.Dto.MessageDto.decode(new Uint8Array(evt.data));
      Logger.fine('decoded');

      switch (msg.message) {
      case 'heartbeat':
      {
        this.network.receiveHeartBeat(msg.heartbeat);
        break;
      }
      case 'commandResult':
      {
        this.network.receiveCommandResult(msg.commandResult);
        break;
      }
      case 'tableEvent':
      {
        this.network.receiveTableEvent(msg.tableEvent);
        break;
      }
      default:
      {
        Logger.error('Unknown message type "' + msg.message + '"');
        break;
      }
      }
    }

    disconnect(eventHandlers) {
      this._forcedClose = true;
      this.socket.onclose = eventHandlers.onSuccess;
      this.socket.close();
    }
    
    sendCommand(cmd) {
      cmd.id = this._commandId;
      this._commandId = this._commandId + 10;
      this.openCommands[cmd.id] = cmd;
    
      Logger.info('Sending command ' + cmd.id + ' - ' + cmd.command);
    
      const {id, command, data} = cmd;
      const payload = {id, command};
        
      const commandDto = ProtoLoader.Dto.CommandDto.create(payload);
      commandDto['.' + cmd.command + 'Command'] = data;
      this.network.sendMessage(commandDto);
      return cmd;
    }
    
    removeOpenCommand(commandId) {
      delete this.openCommands[commandId];
      Logger.fine('Removing command ' + commandId);
    }
}
