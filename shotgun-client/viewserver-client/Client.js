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
    this.network = new Network();
  }

  connect(eventHandlers){
    this.network.connect(this.url, eventHandlers);
  }

  authenticate (type, tokens, eventhandlers) {
    if (Object.prototype.toString.call(tokens) !== '[object Array]') {
        tokens = [tokens];
    }

    let authenticateCommand = new ProtoLoader.Dto.AuthenticateCommandDto();
    authenticateCommand.setType(type);
    authenticateCommand.setToken(tokens);
    this.sendCommand('authenticate', authenticateCommand, false, eventhandlers);
  }

  disconnect(eventHandlers) {
    this.network.disconnect(eventHandlers);
  }

  unsubscribe = function (commandId, eventHandlers) {
    var unsubscribeCommand = new ProtoLoader.Dto.UnsubscribeCommandDto(commandId);
    this.network.removeOpenCommand(commandId);
    return this.sendCommand('unsubscribe', unsubscribeCommand, false, eventHandlers);
  }

  executeSql = function (query, permanent, dataSink) {
      var sqlCommand = new ProtoLoader.Dto.ExecuteSqlCommandDto(query, permanent);
      return this.sendCommand('executeSql', sqlCommand, true, dataSink);
  }

  subscribe = function (operatorName, options, dataSink, output, projection) {
      var optionsDto = OptionsMapper.toDto(options);

      var subscribeCommand = new ProtoLoader.Dto.SubscribeCommandDto(operatorName, output || 'out', optionsDto);
      if (projection !== undefined) {
          subscribeCommand.setProjection(ProjectionMapper.toDto(projection));
      }
      return this.sendCommand('subscribe', subscribeCommand, true, dataSink);
  }

  subscribeToReport = function (reportContext, options, dataSink) {
      var reportContextDto = ReportContextMapper.toDto(reportContext);
      var optionsDto = OptionsMapper.toDto(options);

      var subscribeReportCommand = new ProtoLoader.Dto.SubscribeReportCommandDto();
      subscribeReportCommand.setContext(reportContextDto);
      subscribeReportCommand.setOptions(optionsDto);

      return this.sendCommand('subscribeReport', subscribeReportCommand, true, dataSink);
  }

  subscribeToDimension = function (dimension, reportContext, options, dataSink) {
      var reportContextDto = ReportContextMapper.toDto(reportContext);
      var optionsDto = OptionsMapper.toDto(options);

      var subscribeReportCommand = new ProtoLoader.Dto.SubscribeDimensionCommandDto();
      subscribeReportCommand.setDimension(dimension);
      subscribeReportCommand.setContext(reportContextDto);
      subscribeReportCommand.setOptions(optionsDto);

      return this.sendCommand('subscribeDimension', subscribeReportCommand, true, dataSink);

  }

  updateSubscription = function (commandId, options, eventHandlers) {
      var optionsDto = OptionsMapper.toDto(options);
      var updateSubscriptionCommand = new ProtoLoader.Dto.UpdateSubscriptionCommandDto(commandId, optionsDto);
      return this.sendCommand('updateSubscription', updateSubscriptionCommand, false, eventHandlers);
  }

  editTable = function (operatorName, dataSink, rowEvents, eventHandlers) {
      var tableEvent = new ProtoLoader.Dto.TableEventDto();
      var rowEventDtos = [];
      rowEvents.map(function (index, rowEvent) {
          rowEventDtos.push(RowEventMapper.toDto(rowEvent, dataSink));
      });
      tableEvent.setRowEvents(rowEventDtos);

      let tableEditCommand = new ProtoLoader.Dto.TableEditCommandDto(operatorName, tableEvent);
      return this.sendCommand('tableEdit', tableEditCommand, false, eventHandlers);
  }

  sendCommand = function (commandName, commandDto, continuous, eventHandlers) {
      var command = new Command(commandName, commandDto);
      command.handler = eventHandlers;
      command.continuous = continuous;
      return this.network.sendCommand(command);
  }

}
  