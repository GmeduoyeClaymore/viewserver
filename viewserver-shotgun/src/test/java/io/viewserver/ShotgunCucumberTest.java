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

package io.viewserver;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import io.viewserver.util.dynamic.Spec;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Created by bemm on 12/02/2015.
 */
@Category(Spec.class)
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "src/test/resources"
        },
        glue = {
                "io.viewserver.server.steps"
        },
        format = {
                "junit:target/cucumber-report/junit.xml",
                "html:target/cucumber-report"}
        ,
        tags = {
                "~@ignore"
        }
)
public class ShotgunCucumberTest {
}