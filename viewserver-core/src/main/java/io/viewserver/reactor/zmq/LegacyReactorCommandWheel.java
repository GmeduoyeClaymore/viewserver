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

package io.viewserver.reactor.zmq;

import io.viewserver.reactor.IReactorCommandListener;
import io.viewserver.reactor.ReactorCommand;
import io.viewserver.reactor.ReactorCommandWheelBase;

import java.util.concurrent.ExecutorService;

/**
 * Created by nick on 23/06/15.
 */
public class LegacyReactorCommandWheel extends ReactorCommandWheelBase {
    private final ExecutorService executor;

    public LegacyReactorCommandWheel(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void startRotating() {

    }

    @Override
    public void stopRotating() {

    }

    @Override
    public void pushToWheel(byte command) {
        final ReactorCommand reactorCommand = new ReactorCommand();
        reactorCommand.setOpCode(command);
        executor.execute(() -> {
            for (IReactorCommandListener listener : listeners) {
                listener.onReactorCommand(reactorCommand);
            }
        });
    }
}
