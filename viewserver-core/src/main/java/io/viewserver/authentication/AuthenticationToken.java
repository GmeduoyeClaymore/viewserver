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

package io.viewserver.authentication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 13/02/2015.
 */
public class AuthenticationToken {
    private String type;
    private String id;
    private List<String> authorisedCommands;

    public AuthenticationToken(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void authoriseCommand(String command) {
        if (authorisedCommands == null) {
            authorisedCommands = new ArrayList<>();
        }
        authorisedCommands.add(command);
    }

    public boolean isCommandAuthorised(String command) {
        return authorisedCommands == null || authorisedCommands.contains(command);
    }
}
