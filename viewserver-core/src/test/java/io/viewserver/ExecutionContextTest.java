package io.viewserver;

import io.viewserver.core.ExecutionContext;
import org.junit.Test;

/**
 * Created by Gbemiga on 20/12/17.
 */
public class ExecutionContextTest {

    @Test
    public void Committing_execution_context_causes_each_input_operator_to_increment_its_internal_version(){
        ExecutionContext context = new ExecutionContext();
        context.commit();
    }

}
