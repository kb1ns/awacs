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

package io.awacs.core.transport;

import io.awacs.core.NoSuchKeyTypeException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by pixyonly on 7/12/16.
 */
public abstract class Key<V> implements Serializable {

    protected V value;

    public abstract V value();

    @Override
    public boolean equals(Object key) {
        return (key instanceof Key) && ((Key) key).value().equals(value());
    }

    @Override
    public int hashCode() {
        return ~value().hashCode() << 2;
    }

    @Override
    public String toString() {
        if (value == null)
            return "";
        return value.toString();
    }

    private static ConcurrentMap<String, Constructor<?>> loadedKeys = new ConcurrentHashMap<>();

    public static Key<?> getKey(String keyClass, String keyVal) throws NoSuchKeyTypeException {
        try {
            //FIXME no need to synchronized
            if (loadedKeys.containsKey(keyClass)) {
                return (Key<?>) loadedKeys.get(keyClass).newInstance(keyVal);
            } else {
                Class<?> keyType = Class.forName(keyClass);
                Constructor<?> constructor = keyType.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                loadedKeys.putIfAbsent(keyClass, constructor);
                return (Key<?>) constructor.newInstance(keyVal);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoSuchKeyTypeException();
        }
    }
}
