package io.viewserver.operators.group.summary.json;

import io.viewserver.operators.group.ISummary;
import io.viewserver.operators.group.summary.ISummaryContext;
import io.viewserver.operators.group.summary.ISummaryFactory;
import io.viewserver.operators.group.summary.MultiTypeSummaryBase;

import java.util.stream.Collectors;

import static io.viewserver.core.Utils.fromArray;
import static io.viewserver.core.Utils.toArray;

public class JsonSummaryFactory implements ISummaryFactory {
    public static final String NAME = "json";

    @Override
    public ISummary createSummary(String name, String target, Object[] arguments) {
        String[] sourceColumns = toArray(fromArray(arguments).map(c-> c.toString()).collect(Collectors.toList()), String[]::new);
        return new JsonSummary(name, target, sourceColumns);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static class JsonSummary extends MultiTypeSummaryBase {

        private String[] sourceColumns;

        public JsonSummary(String name, String target, String[] sourceColumns) {
            super(name, target);
            this.sourceColumns = sourceColumns;
        }

        @Override
        public void initialise(ISummaryContext context) {
            this.internalSummary  = new io.viewserver.operators.group.summary.json.JsonSummary(name, target,sourceColumns);
            this.internalSummary.initialise(context);
        }
    }
}