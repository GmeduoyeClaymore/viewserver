import RxDataSink from 'common/dataSinks/RxDataSink';
import ReportSubscriptionStrategy from 'common/subscriptionStrategies/ReportSubscriptionStrategy';


export default class OperatorWithLinksDaoContext{
  constructor(name, options = {}) {
    this.options = options;
    this._name = name;
  }

  get defaultOptions(){
    return {
      offset: 0,
      limit: 1000,
      columnName: undefined,
      columnsToSort: undefined,
      filterMode: 2, //Filtering
      filterExpression: undefined, //Filtering
      flags: undefined,
      operatorPathPrefix: '',
      ...this.options
    };
  }

  getReportContext(options){
    const {operatorName: operatorPath, operatorPathField='path', operatorPathPrefix} = options;
    return {
      reportId: 'operatorsAndConnections',
      parameters: {
        operatorPath,
        operatorPathPrefix,
        operatorPathField
      }
    };
  }

  get name(){
    return this._name;
  }

  createDataSink(){
    return new RxDataSink();
  }

  mapDomainEvent(event, dataSink){
    return this.transformToLinksAndNodes(dataSink.rows);
  }

  transformToLinksAndNodes(rows){
    const links = [];
    const nodesByName = {};
    const linksByKey = {};

    //rows = rows.filter(rw => !rw.type.includes("Serialiser"));

    rows.forEach(rw => {
      nodesByName[rw.nodeName] = this.processNode(rw)
    });

    const getNodeByName = (name) => {
      if(!name){
        throw new Error("Cannot get node with null name")
      }
      let result = nodesByName[name];
      if(!result){
          result = {id: name};
          result.key = result.id;
          result.radius = 0;
          result.color = 'white';
          result.data = {};
          nodesByName[name] = result;
      }
      return result;
    }

    const getLink = (targetOperator,sourceOperator) => {
      if(!sourceOperator || !targetOperator){
        return null;
      }
      if(sourceOperator === targetOperator){
        return null;
      }
      const link = {key: sourceOperator+ targetOperator, source: getNodeByName(sourceOperator), target: getNodeByName(targetOperator)};
    linksByKey[link.key] = link;
      return link;
    }

    rows.forEach(rw => {
      nodesByName[rw.nodeName] = this.processNode(rw)
    });

    rows.forEach(rw => {
      getLink(rw["input_inputOperator"],rw["input_outputOperator"]);
      getLink(rw["output_inputOperator"],rw["output_outputOperator"]);
    });

    return {
      links: Object.values(linksByKey),
      nodes: Object.values(nodesByName)
    };
  }

  processNode(row){
    const result = {};
    const data = {};
    Object.keys(row).forEach(
      key => {
        if(!(key.startsWith("output_") || key.startsWith("input_"))){
          data[key] = row[key];
        }
      }
    );
    result.id = row.nodeName;
    result.key = result.id;
    result.radius = 10;
    result.data = data;
    return result;
  }

  createSubscriptionStrategy(client, options, dataSink){
    return new ReportSubscriptionStrategy(client, this.getReportContext(options), dataSink);
  }

  doesSubscriptionNeedToBeRecreated(previousOptions, newOptions){
    return !previousOptions || previousOptions.operatorName != newOptions.operatorName || previousOptions.operatorPathField != newOptions.operatorPathField;
  }

  extendDao(dao){
    dao._observable = dao.subject.throttleTime(100)
  }

  transformOptions(options){
    const {operatorName} = options;
    if (!operatorName ){
      throw new Error('operatorName should be defined');
    }
    return {...options};
  }
}


