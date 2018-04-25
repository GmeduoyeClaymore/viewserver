package io.viewserver.operators.spread;

public interface ISpreadConfig {
    String getInputColumnName();
    String spreadFunctionName();
    boolean removeInputColumn();
}
