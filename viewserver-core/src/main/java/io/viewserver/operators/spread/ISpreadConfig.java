package io.viewserver.operators.spread;

public interface ISpreadConfig {
    String getInputColumnName();
    String getOutputColumnName();
    String spreadFunctionName();
    boolean removeInputColumn();
}
