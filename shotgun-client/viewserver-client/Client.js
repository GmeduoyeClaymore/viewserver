import Network from './Network';
import Command from './Command';
import ReportContextMapper from './mappers/ReportContextMapper';
import OptionsMapper from './mappers/OptionsMapper';
import RowEventMapper from './mappers/RowEventMapper';
import ProjectionMapper from './mappers/ProjectionMapper';
import ProtoLoader from './core/ProtoLoader';

export default class Client {
  
  constructor(url,protocol) {
    this.url = url;
    this.protocol = protocol;
    this.network = new Network(this.url);
  }

  connect(eventHandlers){
    return this.network.connect(eventHandlers);
  }

  authenticate (type, tokens, eventhandlers) {
    if (Object.prototype.toString.call(tokens) !== '[object Array]') {
        tokens = [tokens];
    }

    let authenticateCommand = ProtoLoader.Dto.AuthenticateCommandDto.create();
    authenticateCommand.setType(type);
    authenticateCommand.setToken(tokens);
    this.sendCommand('authenticate', authenticateCommand, false, eventhandlers);
  }
 
  disconnect(eventHandlers) {
    this.network.disconnect(eventHandlers);
  }

  unsubscribe = function (commandId, eventHandlers) {
    var unsubscribeCommand = ProtoLoader.Dto.UnsubscribeCommandDto.create({commandId});
    this.network.removeOpenCommand(commandId);
    return this.sendCommand('unsubscribe', unsubscribeCommand, false, eventHandlers);
  }

  executeSql = function (query, permanent, dataSink) {
      var sqlCommand = ProtoLoader.Dto.ExecuteSqlCommandDto.create({query, permanent});
      return this.sendCommand('executeSql', sqlCommand, true, dataSink);
  }

  subscribe = function (operatorName, options, dataSink, output, projection) {
      var optionsDto = OptionsMapper.toDto(options);

      var subscribeCommand = ProtoLoader.Dto.SubscribeCommandDto.create({operatorName, output  : output || 'out', options : optionsDto});
      if (projection !== undefined) {
          subscribeCommand.setProjection(ProjectionMapper.toDto(projection));
      }
      return this.sendCommand('subscribe', ProtoLoader.Dto.SubscribeCommandDto.encode(subscribeCommand), true, dataSink);
  }

  subscribeToReport = function (reportContext, options, dataSink) {
      var reportContextDto = ReportContextMapper.toDto(reportContext);
      var optionsDto = OptionsMapper.toDto(options);

      var subscribeReportCommand = ProtoLoader.Dto.SubscribeReportCommandDto.create({
          context : reportContextDto,
          options : optionsDto
      });

      return this.sendCommand('subscribeReport', subscribeReportCommand, true, dataSink);
  }

  subscribeToDimension = function (dimension, reportContext, options, dataSink) {
      var context = ReportContextMapper.toDto(reportContext);
      var optionsDto = OptionsMapper.toDto(options);

      var subscribeReportCommand = ProtoLoader.Dto.SubscribeDimensionCommandDto.create({
        dimension,
        context,
        options : optionsDto
      });

      return this.sendCommand('subscribeDimension', subscribeReportCommand, true, dataSink);

  }

  updateSubscription = function (commandId, options, eventHandlers) {
      var optionsDto = OptionsMapper.toDto(options);
      var updateSubscriptionCommand = ProtoLoader.Dto.UpdateSubscriptionCommandDto.create({
          commandId, 
          options : optionsDto
        });
      return this.sendCommand('updateSubscription', updateSubscriptionCommand, false, eventHandlers);
  }

  editTable = function (operatorName, dataSink, rowEvents, eventHandlers) {
      
      var rowEventDtos = [];
      rowEvents.map(function (index, rowEvent) {
          rowEventDtos.push(RowEventMapper.toDto(rowEvent, dataSink));
      });
      let tableEvent = ProtoLoader.Dto.TableEventDto.create({
        rowEvents : rowEventDtos
      })

      let tableEditCommand = ProtoLoader.Dto.TableEditCommandDto.create({operatorName, tableEvent});
      return this.sendCommand('tableEdit', tableEditCommand, false, eventHandlers);
  }

  sendCommand = function (commandName, commandDto, continuous, eventHandlers) {
      var command = new Command(commandName, commandDto);
      command.handler = eventHandlers;
      command.continuous = continuous;
      return this.network.sendCommand(command);
  }

}
  