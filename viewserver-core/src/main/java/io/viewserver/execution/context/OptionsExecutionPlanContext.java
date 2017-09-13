/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.execution.context;

import io.viewserver.execution.Options;
import io.viewserver.operators.projection.IProjectionConfig;


public class OptionsExecutionPlanContext extends ExecutionPlanContextBase {
    private Options options;
    private IProjectionConfig projectionConfig;

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public IProjectionConfig getProjectionConfig() {
        return projectionConfig;
    }

    public void setProjectionConfig(IProjectionConfig projectionConfig) {
        this.projectionConfig = projectionConfig;
    }
}
