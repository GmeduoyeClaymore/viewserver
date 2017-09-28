import ProtoBuf from "protobufjs";
import Logger from '../Logger';
import fetch from '@protobufjs/fetch';



export default class ProtoLoader{
    constructor(){
    }

    static _dto = {};
    static _path = 'http://192.168.0.20:19001/viewserver-client/core/proto/';

    static get Dto(){
        return ProtoLoader._dto
    }

    static async loadAll(){
        var root = ProtoBuf.Root.fromJSON(require("./proto/bundle.json"));
        ProtoLoader._dto.OptionsDto = root.lookupType('OptionsDto');
        ProtoLoader._dto.CommandDto = root.lookupType('CommandDto');
        ProtoLoader._dto.MessageDto = root.lookupType('MessageDto');
        ProtoLoader._dto.SubscribeCommandDto = root.lookupType('SubscribeCommandDto');
        ProtoLoader._dto.UnsubscribeCommandDto = root.lookupType('UnsubscribeCommandDto');
        ProtoLoader._dto.SubscribeReportCommandDto = root.lookupType('SubscribeReportCommandDto');
        ProtoLoader._dto.SubscribeDimensionCommandDto = root.lookupType('SubscribeDimensionCommandDto');
        ProtoLoader._dto.UpdateSubscriptionCommandDto = root.lookupType('UpdateSubscriptionCommandDto');
        ProtoLoader._dto.ReportContextDto = root.lookupType('ReportContextDto');
        ProtoLoader._dto.AuthenticateCommandDto = root.lookupType('AuthenticateCommandDto');
        ProtoLoader._dto.TableEditCommandDto = root.lookupType('TableEditCommandDto');
        ProtoLoader._dto.ProjectionConfigDto = root.lookupType('ProjectionConfigDto');
        ProtoLoader._dto.ExecuteSqlCommandDto = root.lookupType('ExecuteSqlCommandDto');
    }

}