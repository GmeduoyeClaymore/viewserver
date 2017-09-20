import ProtoBuf from "protobufjs";
export default class ProtoLoader{
    constructor(){
        this.path = '../proto/';
        this._dto = await this.loadAll();
    }

    get Dto(){
        return this._dto
    }

    async loadAll(){
        let result = {};
        result.MessageDto = await ProtoBuf.load(this.path + 'MessageDto.proto');
        result.SubscribeCommandDto = await ProtoBuf.load(this.path + 'SubscribeCommandDto.proto');
        result.UnsubscribeCommandDto = await ProtoBuf.load(this.path + 'UnsubscribeCommandDto.proto');
        result.SubscribeReportCommandDto = await ProtoBuf.load(this.path + 'SubscribeReportCommandDto.proto');
        result.SubscribeDimensionCommandDto = await ProtoBuf.load(this.path + 'SubscribeDimensionCommandDto.proto');
        result.UpdateSubscriptionCommandDto = await ProtoBuf.load(this.path + 'UpdateSubscriptionCommandDto.proto');
        result.ReportContextDto = await ProtoBuf.load(this.path + 'ReportContextDto.proto');
        result.AuthenticateCommandDto = await ProtoBuf.load(this.path + 'AuthenticateCommandDto.proto');
        result.TableEditCommandDto = await ProtoBuf.load(this.path + 'TableEditCommandDto.proto');
        result.ProjectionConfigDto = await ProtoBuf.load(this.path + 'ProjectionConfigDto.proto');
        result.ExecuteSqlCommandDto = await ProtoBuf.load(this.path + 'ExecuteSqlCommandDto.proto');
        return result;
    }

}