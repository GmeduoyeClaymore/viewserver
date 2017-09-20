import Logger from './Logger';
import ProtoLoader from './core/ProtoLoader';

export default class Connection {
    constructor(uri, eventHandlers, network) {
        this.eventHandlers = eventHandlers;
        this.network = network;
        this.socket = new WebSocket(uri);
        this.socket.binaryType = 'arraybuffer';
        this.socket.onopen = this.handleSocketOpen;
        this.socket.onclose = this.handleSocketClosed;
        this.commandId = 0;
        this.openCommands = [];
    }

    get socket(){
        return this.socket;
    }
    
    get openCommands(){
        return this.openCommands;
    }

    handleSocketOpen(){
        Logger.debug('Network Connected');
        this.eventHandlers.onSuccess();
    }

    handleSocketClosed(e){
        Logger.debug('Network Disconnected');
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

        if(e.code > 1000 && e.wasClean === false){
            this.eventHandlers.onError(reason);
        }
    }

    handleSocketMessage(evt)
    {
        Logger.fine('Network Received msg');
        if (evt.data.byteLength === 0) {
            return;
        }

        Logger.fine('about to decode');
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
    
    sendCommand(command) {
        command.id = this.commandId++;
        this.openCommands[command.id] = command;
    
        Logger.info('Sending command ' + command.id + ' - ' + command.command);
        Logger.fine(JSON.stringify(command.data));
    
    
        let commandDto = new ProtoLoader.Dto.CommandDto(command.id, command.command);
    //            commandDto.data = command.data.encode();
        commandDto.set('.' + command.command + 'Command', command.data);
        this.sendMessage(commandDto);
        return command;
    }
    
    removeOpenCommand(commandId) {
        delete this.openCommands[commandId];
        Logger.fine('Removing command ' + commandId);
    }
}
