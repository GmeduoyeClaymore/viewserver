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

    const getNodeByName = (name,type) => {
      if(!name){
        throw new Error("Cannot get node with null name")
      }
      let result = nodesByName[name];
      if(!result){
          result = {id: name};
          result.key = result.id;
          result.radius = 20;
          result.color = '#D3D3D3';
          result.data = {};
          result.data.type = type;
          nodesByName[name] = result;
      }
      return result;
    }

    const getLink = (targetOperatorName,sourceOperatorName,row) => {
      
      const targetOperator = row[targetOperatorName];
      const targetOperatorType = row[targetOperatorName + 'Type'];
      const sourceOperator = row[sourceOperatorName];
      const sourceOperatorType = row[sourceOperatorName + 'Type'];

      if(!sourceOperator || !targetOperator){
        return null;
      }
      if(sourceOperator === targetOperator){
        return null;
      }
      const link = {key: sourceOperator+ targetOperator, source: getNodeByName(sourceOperator,sourceOperatorType), target: getNodeByName(targetOperator,targetOperatorType)};
      linksByKey[link.key] = link;
      return link;
    }

    const processAdditionalNode = (row, nodeNameField) => {
      if(!row[nodeNameField]){
        return;
      }
      if(nodesByName[nodeNameField]){
        return;
      }

    }

    rows.forEach(rw => {
      nodesByName[rw.nodeName] = this.processNode(rw)
    });

    rows.forEach(rw => {
      getLink("input_inputOperator" , "input_outputOperator", rw);
      getLink("output_inputOperator" , "output_outputOperator", rw);
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
    result.radius = 20;
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


