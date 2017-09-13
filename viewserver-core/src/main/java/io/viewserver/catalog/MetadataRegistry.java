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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nick on 10/03/2015.
 */
public class MetadataRegistry {
    private final Set<IInput> inputs = new HashSet<>();
    private final Set<IOutput> outputs = new HashSet<>();
    private final Set<Link> links = new HashSet<>();
    private final List<IMetadataListener> listeners = new ArrayList<>();

    public void registerInput(IInput input) {
        for (IMetadataListener listener : listeners) {
            listener.onRegisterInput(input);
        }

        inputs.add(input);
    }

    public void unregisterInput(IInput input) {
        for (IMetadataListener listener : listeners) {
            listener.onUnregisterInput(input);
        }

        inputs.remove(input);
    }

    public void registerOutput(IOutput output) {
        for (IMetadataListener listener : listeners) {
            listener.onRegisterOutput(output);
        }

        outputs.add(output);
    }

    public void unregisterOutput(IOutput output) {
        for (IMetadataListener listener : listeners) {
            listener.onUnregisterOutput(output);
        }

        outputs.remove(output);
    }

    public void registerLink(IOutput output, IInput input) {
        for (IMetadataListener listener : listeners) {
            listener.onRegisterLink(output, input);
        }

        links.add(new Link(output, input));
    }

    public void unregisterLink(IOutput output, IInput input) {
        for (IMetadataListener listener : listeners) {
            listener.onUnregisterLink(output, input);
        }

        links.remove(new Link(output, input));
    }

    public void addListener(IMetadataListener listener, boolean catchUp) {
        listeners.add(listener);
        if (catchUp) {
            for (IOutput output : outputs) {
                listener.onRegisterOutput(output);
            }
            for (IInput input : inputs) {
                listener.onRegisterInput(input);
            }
            for (Link link : links) {
                listener.onRegisterLink(link.getOutput(), link.getInput());
            }
        }
    }

    public void removeListener(IMetadataListener listener) {
        listeners.remove(listener);
    }

}
