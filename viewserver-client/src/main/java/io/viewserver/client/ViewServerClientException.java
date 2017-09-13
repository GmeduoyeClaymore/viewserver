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

package io.viewserver.client;

/**
 * Created by nick on 18/03/2015.
 */
public class ViewServerClientException extends RuntimeException {
    public ViewServerClientException() {
    }

    public ViewServerClientException(String message) {
        super(message);
    }

    public ViewServerClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ViewServerClientException(Throwable cause) {
        super(cause);
    }

    public ViewServerClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
