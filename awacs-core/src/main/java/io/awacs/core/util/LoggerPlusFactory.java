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

package io.awacs.core.util;

/**
 * Created by pixyonly on 16/10/13.
 */
public abstract class LoggerPlusFactory {

    private static volatile LoggerPlusFactory defaultFactory;

    static {
        try {
            Class.forName("org.slf4j.Logger");
            defaultFactory = new Sl4jLoggerPlus.Sl4jLoggerPlusFactory();
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("java.util.logging.Logger");
                defaultFactory = new InternalLoggerPlus.InternalLoggerPlusFactory();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }

    protected abstract LoggerPlus getInstance(String name);

    protected abstract LoggerPlus getInstance(Class<?> clazz);

    public static LoggerPlus getLogger(String name) {
        return defaultFactory.getInstance(name);
    }

    public static LoggerPlus getLogger(Class<?> clazz) {
        return defaultFactory.getInstance(clazz);
    }
}
