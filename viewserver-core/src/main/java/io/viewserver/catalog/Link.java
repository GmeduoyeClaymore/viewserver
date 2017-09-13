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

package io.viewserver.catalog;

import io.viewserver.operators.IInput;
import io.viewserver.operators.IOutput;

/**
* Created by nick on 10/03/2015.
*/
class Link {
    private IOutput output;
    private IInput input;

    public Link(IOutput output, IInput input) {
        this.output = output;
        this.input = input;
    }

    public IOutput getOutput() {
        return output;
    }

    public IInput getInput() {
        return input;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (!input.equals(link.input)) return false;
        if (!output.equals(link.output)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = output.hashCode();
        result = 31 * result + input.hashCode();
        return result;
    }
}
