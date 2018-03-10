package io.viewserver.execution.nodes;

import io.viewserver.datasource.IDataSource;
import io.viewserver.execution.ParameterHelper;

public class UserUnEnumNode extends UnEnumNode {

    public UserUnEnumNode(String name, IDataSource dataSource) {
        super(name,dataSource);
    }

    @Override
    public String getOperatorName(ParameterHelper parameterHelper) {
        return getName();
    }
}
