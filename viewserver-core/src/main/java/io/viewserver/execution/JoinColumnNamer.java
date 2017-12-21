package io.viewserver.execution;

import io.viewserver.operators.join.IColumnNameResolver;

/**
 * Created by nickc on 31/10/2014.
 */

public class JoinColumnNamer implements IColumnNameResolver {
    private String leftPrefix = "";
    private String rightPrefix = "";

    public JoinColumnNamer(String leftPrefix, String rightPrefix) {
        this.leftPrefix = leftPrefix != null ? leftPrefix : "";
        this.rightPrefix = rightPrefix != null ?  rightPrefix : "";
    }

    public JoinColumnNamer(String leftPrefix) {
        this.leftPrefix = leftPrefix;
        this.rightPrefix = "";
    }

    @Override
    public String resolveColumnName(String originalName, boolean isLeft) {
        return String.format("%s%s", isLeft ? leftPrefix : rightPrefix, originalName);
    }

    public String getLeftPrefix() {
        return leftPrefix;
    }

    public String getRightPrefix() {
        return rightPrefix;
    }
}
