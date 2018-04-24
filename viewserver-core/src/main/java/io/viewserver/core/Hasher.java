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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.security.MessageDigest.getInstance;

/**
 * Created by bemm on 31/10/2014.
 */
public class Hasher {
    private static Logger log = LoggerFactory.getLogger(Hasher.class);
    private static final char[] hexArray = "0123456789abcdef".toCharArray();
    private static final Map<String, String> reverseLookupMap = new ConcurrentHashMap<String, String>();

    public static String SHA1(String input) {
        final MessageDigest digest;
        try {
            digest = getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        digest.update(input.getBytes(Charset.forName("UTF-8")));
        String hash = bytesToHex(digest.digest());
        reverseLookupMap.put(hash, input);
        if (log.isTraceEnabled()) {
            log.trace("{} - {}", hash, input);
        }
        return hash;
    }

    public static String reverseLookup(String hash) {
        return reverseLookupMap.containsKey(hash) ? reverseLookupMap.get(hash) : null;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int x;
        for (int i = 0; i < bytes.length; i++) {
            x = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[x >>> 4];
            hexChars[i * 2 + 1] = hexArray[x & 0x0F];
        }
        return new String(hexChars);
    }
}
