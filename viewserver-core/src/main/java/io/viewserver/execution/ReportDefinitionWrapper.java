package io.viewserver.execution;

import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.report.*;

import java.util.List;
import java.util.Map;

public class ReportDefinitionWrapper implements IGraphDefinition, IParameterisedGraphDefinition, ICalculatingGraphDefinition, IMeasuringGraphDefinition  {
    private ReportDefinition reportDefinition;
    private ReportContext reportContext;

    public ReportDefinitionWrapper(ReportDefinition reportDefinition, ReportContext reportContext) {
        this.reportDefinition = reportDefinition;
        this.reportContext = reportContext;
    }
    @Override
    public List<IGraphNode> getNodes() {
        IGraphNodeRetriever retriever = reportDefinition.getGraphNodeRetriever();
        if(retriever == null){
            return reportDefinition.getNodes();
        }
        return retriever.transform(reportDefinition,reportContext);
    }

    @Override
    public String getOutput() {
        return reportDefinition.getOutput();
    }

    @Override
    public void setOutput(String output) {
        reportDefinition.setOutput(output);
    }

    @Override
    public Map<String, CalculationDefinition> getCalculations() {
        return reportDefinition.getCalculations();
    }

    @Override
    public Map<String, MeasureDefinition> getMeasures() {
        return reportDefinition.getMeasures();
    }

    @Override
    public Map<String, ParameterDefinition> getParameters() {
        return reportDefinition.getParameters();
    }
}

