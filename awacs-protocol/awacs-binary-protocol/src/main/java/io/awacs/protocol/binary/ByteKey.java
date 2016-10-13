/**
 * Copyright 2016 AWACS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.awacs.protocol.binary;

import io.awacs.core.transport.Key;
import io.awacs.core.NoSuchKeyTypeException;

/**
 * Created by pixyonly on 7/12/16.
 */
public class ByteKey extends Key<Byte> {

    private ByteKey(String value) {
        this(Byte.valueOf(value));
    }

    private ByteKey(byte value) {
        super.value = value;
    }

    @Override
    public Byte value() {
        return value;
    }

    public static ByteKey getKey(String keyClass, String keyVal) throws NoSuchKeyTypeException {
        if(!ByteKey.class.getCanonicalName().equals(keyClass))
            throw new NoSuchKeyTypeException();
        return new ByteKey(keyVal);
    }

    public static ByteKey getKey(byte val) {
        return new ByteKey(val);
    }
}
