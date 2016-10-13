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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pixyonly on 16/10/13.
 */
class Sl4jLoggerPlus implements LoggerPlus {

    private Logger logger;

    private Sl4jLoggerPlus(Logger logger) {
        this.logger = logger;
    }

    /**
     *
     */
    @Override
    public String name() {
        return logger.getName();
    }

    /**
     *
     */
    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    /**
     * @param msg
     */
    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    /**
     * @param format
     * @param arguments
     */
    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    /**
     * @param msg
     * @param t
     */
    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
    }

    /**
     *
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * @param msg
     */
    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    /**
     * @param format
     * @param arguments
     */
    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    /**
     * @param msg
     * @param t
     */
    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    /**
     *
     */
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * @param msg
     */
    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    /**
     * @param format
     * @param arguments
     */
    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    /**
     * @param msg
     * @param t
     */
    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    /**
     *
     */
    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    /**
     * @param msg
     */
    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    /**
     * @param format
     * @param arguments
     */
    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    /**
     * @param msg
     * @param t
     */
    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    /**
     *
     */
    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    /**
     * @param msg
     */
    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    /**
     * @param format
     * @param arguments
     */
    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    /**
     * @param msg
     * @param t
     */
    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    /**
     * Created by pixyonly on 16/10/13.
     */
    static class Sl4jLoggerPlusFactory extends LoggerPlusFactory {

        @Override
        protected LoggerPlus getInstance(String name) {
            return new Sl4jLoggerPlus(LoggerFactory.getLogger(name));
        }

        @Override
        protected LoggerPlus getInstance(Class<?> clazz) {
            return new Sl4jLoggerPlus(LoggerFactory.getLogger(clazz));
        }
    }
}
