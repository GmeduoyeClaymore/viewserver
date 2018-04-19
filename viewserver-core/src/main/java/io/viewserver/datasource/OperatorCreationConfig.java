package io.viewserver.datasource;

public class OperatorCreationConfig {
    private CreationStrategy operator;
    private CreationStrategy catalog;

    public OperatorCreationConfig(CreationStrategy operator, CreationStrategy catalog) {
        this.operator = operator;
        this.catalog = catalog;
    }

    public CreationStrategy getOperator() {
        return operator;
    }

    public CreationStrategy getCatalog() {
        return catalog;
    }
}

