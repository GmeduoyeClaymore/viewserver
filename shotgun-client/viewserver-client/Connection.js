import Logger from './Logger';
import ProtoLoader from './core/ProtoLoader';

export default class Connection {
    constructor(uri, eventHandlers, network) {
        this._uri = uri;
        this._eventHandlers = eventHandlers;
        this._network = network;
        this._commandId = 0;
        this._openCommands = [];
        this._isOpen = false;
        this.handleSocketOpen = this.handleSocketOpen.bind(this);
        this.handleSocketClosed = this.handleSocketClosed.bind(this);
        this.handleSocketMessage = this.handleSocketMessage.bind(this);
    }


    static CONNECTION_ATTEMPT_DELAY = 5000; // The maximum amount of time to wait for the socket to open before giving up

    connect(){
        let _this = this;
        _this._socket = new WebSocket(this._uri);
        _this._socket.binaryType = 'arraybuffer';
        _this._socket.onopen = this.handleSocketOpen;
        _this._socket.onclose = this.handleSocketClosed;
        _this._socket.onmessage = this.handleSocketMessage;
        return new Promise((resolve, reject) => {
            const retryFrequency = 10;
            const retryInterval = Connection.CONNECTION_ATTEMPT_DELAY/retryFrequency;
            const result = (counter) => {
                if(_this._isOpen){
                    resolve();
                }else if(counter === retryFrequency){
                    reject( `Unable to detect open web socket to ${_this._uri} after ${retryFrequency} retries. Total time elapsed ${Connection.CONNECTION_ATTEMPT_DELAY} ms`)
                }else{
                    Logger.debug(`Attempt ${counter} socket not connected. Socket state is ${_this._socket.readyState} Trying again in  ${retryInterval}ms`)
                    setTimeout(() => result(counter+1),
                        retryInterval
                    );
                }
                
            }
            result(0);
        });
        
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
        if(this._eventHandlers){
            this._eventHandlers.onSuccess();
        }
    }

    handleSocketClosed(e){
        
        let reason;

        switch(e.code){
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

        if(e.code > 1000 && e.wasClean === false){
            if(this._eventHandlers){
                this._eventHandlers.onError(reason);
            }
        }
    }

    handleSocketMessage(evt)
    {
        Logger.fine('Network Received msg');
        if (evt.data.byteLength === 0) {
            return;
        }

  
        let msg = ProtoLoader.Dto.MessageDto.decode(evt.data);
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
        this.socket.onclose = eventHandlers.onSuccess;
        this.socket.close();
    }
    
    sendCommand(cmd) {
        cmd.id = this.commandId++;
        this.openCommands[cmd.id] = cmd;
    
        Logger.info('Sending command ' + cmd.id + ' - ' + cmd.command + ' - ' + JSON.stringify(cmd.data));
        Logger.fine(JSON.stringify(cmd.data));
    
        const {id,command,data} = cmd;
        const payload = {id,command};
        
        let commandDto = ProtoLoader.Dto.CommandDto.create(payload);
        commandDto["." + cmd.command + 'Command'] = data; 
        Logger.info('constructed - ' + JSON.stringify(commandDto));
        this.network.sendMessage(commandDto);
        return command;
    }
    
    removeOpenCommand(commandId) {
        delete this.openCommands[commandId];
        Logger.fine('Removing command ' + commandId);
    }
}
