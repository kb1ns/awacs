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


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 16/10/13.
 */
class InternalLoggerPlus implements LoggerPlus {

    private Logger logger;

    private InternalLoggerPlus(Logger logger) {
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
        return logger.isLoggable(Level.FINE);
    }

    /**
     * @param msg
     */
    @Override
    public void trace(String msg) {
        logger.fine(msg);
    }

    /**
     * @param format
     * @param arguments
     */
    @Override
    public void trace(String format, Object... arguments) {
        logger.log(Level.FINE, format, arguments);
    }

    /**
     * @param msg
     * @param t
     */
    @Override
    public void trace(String msg, Throwable t) {
        logger.log(Level.FINE, msg, t);
    }

    /**
     *
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.CONFIG);
    }

    /**
     * @param msg
     */
    @Override
    public void debug(String msg) {
        logger.config(msg);
    }

    /**
     * @param format
     * @param arguments
     */
    @Override
    public void debug(String format, Object... arguments) {
        logger.log(Level.CONFIG, format, arguments);
    }

    /**
     * @param msg
     * @param t
     */
    @Override
    public void debug(String msg, Throwable t) {
        logger.log(Level.CONFIG, msg, t);
    }

    /**
     *
     */
    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
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
        logger.log(Level.INFO, format, arguments);
    }

    /**
     * @param msg
     * @param t
     */
    @Override
    public void info(String msg, Throwable t) {
        logger.log(Level.INFO, msg, t);
    }

    /**
     *
     */
    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    /**
     * @param msg
     */
    @Override
    public void warn(String msg) {
        logger.warning(msg);
    }

    /**
     * @param format
     * @param arguments
     */
    @Override
    public void warn(String format, Object... arguments) {
        logger.log(Level.WARNING, format, arguments);
    }

    /**
     * @param msg
     * @param t
     */
    @Override
    public void warn(String msg, Throwable t) {
        logger.log(Level.WARNING, msg, t);
    }

    /**
     *
     */
    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    /**
     * @param msg
     */
    @Override
    public void error(String msg) {
        logger.severe(msg);
    }

    /**
     * @param format
     * @param arguments
     */
    @Override
    public void error(String format, Object... arguments) {
        logger.log(Level.SEVERE, format, arguments);
    }

    /**
     * @param msg
     * @param t
     */
    @Override
    public void error(String msg, Throwable t) {
        logger.log(Level.SEVERE, msg, t);
    }

    /**
     * Created by pixyonly on 16/10/13.
     */
    static class InternalLoggerPlusFactory extends LoggerPlusFactory {

        @Override
        protected LoggerPlus getInstance(String name) {
            return new InternalLoggerPlus(Logger.getLogger(name));
        }

        @Override
        protected LoggerPlus getInstance(Class<?> clazz) {
            return new InternalLoggerPlus(Logger.getLogger(clazz.getName()));
        }

    }
}
