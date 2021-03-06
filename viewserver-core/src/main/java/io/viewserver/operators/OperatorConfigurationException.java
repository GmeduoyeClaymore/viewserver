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

package io.viewserver.operators;


public class OperatorConfigurationException extends RuntimeException {
    private IOperator operator;

    public OperatorConfigurationException(IOperator operator, String message) {
        super(String.format("Configuration error in operator %s - %s", operator.getName(), message));
        this.operator = operator;
    }

    public OperatorConfigurationException(IOperator operator, Throwable cause) {
        super(String.format("Configuration error in operator %s", operator.getName()), cause);
        this.operator = operator;
    }

    public OperatorConfigurationException(IOperator operator, Throwable cause, String message) {
        super(String.format("Configuration error in operator %s - %s", operator.getName(),message), cause);
        this.operator = operator;
    }
    public IOperator getOperator() {
        return operator;
    }

    @Override
    public String getMessage() {
        if (getCause() != null) {
            return getCause().getMessage();
        } else {
            return super.getMessage();
        }
    }
}
