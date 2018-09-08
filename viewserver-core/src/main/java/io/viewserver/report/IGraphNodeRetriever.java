package io.viewserver.report;

import io.viewserver.execution.ReportContext;
import io.viewserver.execution.nodes.IGraphNode;

import java.util.List;

/**
 * Created by bemm on 31/10/2014.
 */

public interface IGraphNodeRetriever{
    default List<IGraphNode> transform(ReportDefinition definition, ReportContext context){
        return definition.getNodes();
    }
}
