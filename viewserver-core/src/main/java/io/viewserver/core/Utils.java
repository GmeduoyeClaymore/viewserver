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

package io.viewserver.core;

import com.google.common.base.Charsets;
import com.sun.deploy.Environment;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by nick on 10/09/15.
 */
public class Utils {
    // TODO: get rid of the static variable
    public static Configuration Configuration = new SystemConfiguration();
    private static final long seed = 0xfeedcabedeadbeefL;
    public static final String PARSE_KEY = new String(new byte[] { 101, 110, 99, 114, 121, 112, 116, 101, 100 });

    public static String replaceSystemTokens(String inputString) {
        String result = inputString;
        Pattern pattern = Pattern.compile("%([^%]+)%");
        Matcher matcher = pattern.matcher(inputString);
        while (true) {
            boolean found = false;
            while (matcher.find()) {
                found = true;
                String token = matcher.group(1);
                String value = Configuration.getString(token);

                if(value == null){
                    value = System.getProperty(token);
                }
                if(value == null){
                    value = System.getenv(token);
                }
                if(value == null){
                    throw new RuntimeException(String.format("Unable to find configuration for token %s in %s",token, inputString));
                }
                if (value != null) {
                    boolean encrypted = Configuration.getBoolean(String.format("%s[@%s]", token, PARSE_KEY), false);
                    if (encrypted) {
                        value = parse(value);
                    }
                    result = result.replace(matcher.group(0), value);
                }
            }
            if (!found) {
                break;
            }
            matcher.reset(result);
        }
        //only replace single backslashes with double backslashes
        return result.replaceAll(Matcher.quoteReplacement("(?<!\\)\\(?![\\\"\'])"), Matcher.quoteReplacement("\\\\"));
    }

    public static String parse(String value) {
        return new String(getBytes(Base64.getDecoder().decode(value)), Charsets.UTF_8);
    }

    public static String serialise(String value) {
        return new String(Base64.getEncoder().encode(getBytes(value.getBytes(Charsets.UTF_8))), Charsets.UTF_8);
    }

    public static String[] splitIgnoringEmpty(String name, String delimiter) {
        if(name == null || name.isEmpty()){
            return new String[0];
        }
        List<String> result = Arrays.asList(name.split(delimiter));
        List<String> collect = result.stream().filter(c -> c != null && !c.isEmpty()).collect(Collectors.toList());
        return collect.toArray(new String[collect.size()]);
    }


    private static byte[] getBytes(byte[] input) {
        Random random = new Random(seed);
        int length = input.length;
        byte[] output = new byte[length];
        for (int i = 0; i < length; i++) {
            output[i] = (byte) (input[i] ^ random.nextInt(256));
        }
        return output;
    }

    public static void main(String[] args) {
        System.out.println(serialise(args[0]));
    }
}
