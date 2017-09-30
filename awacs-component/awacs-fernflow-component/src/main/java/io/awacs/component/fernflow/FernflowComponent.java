package io.awacs.component.fernflow;

import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.Releasable;
import io.awacs.component.org.jetbrains.java.decompiler.main.DecompilerContext;
import io.awacs.component.org.jetbrains.java.decompiler.main.Fernflower;
import io.awacs.component.org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import io.awacs.component.org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import io.awacs.component.org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Copyright 2016-2017 AWACS Project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * Created by pixyonly on 30/09/2017.
 */
public class FernflowComponent implements Configurable, Releasable {

    private static Logger log = LoggerFactory.getLogger(FernflowComponent.class);

    private Fernflower fernflower;

    private byte[] bytecode;

    private String root;

    @Override
    public void init(Configuration configuration) {
        IBytecodeProvider bytecodeInput = (externalPath, internalPath) -> bytecode;
        IResultSaver sourcecodeOutput = new IResultSaver() {

            @Override
            public void saveFolder(String path) {
                File dir = new File(getAbsolutePath(path));
                if (!(dir.mkdirs() || dir.isDirectory())) {
                    throw new RuntimeException("Cannot create directory " + dir);
                }
            }

            @Override
            public void copyFile(String source, String path, String entryName) {

            }

            @Override
            public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
                File file = new File(getAbsolutePath(path), entryName);
                try (Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF8")) {
                    out.write(content);
                } catch (IOException ex) {
                    DecompilerContext.getLogger().writeMessage("Cannot write class file " + file, ex);
                }
            }

            @Override
            public void createArchive(String path, String archiveName, Manifest manifest) {

            }

            @Override
            public void saveDirEntry(String path, String archiveName, String entryName) {

            }

            @Override
            public void copyEntry(String source, String path, String archiveName, String entry) {

            }

            @Override
            public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {

            }

            @Override
            public void closeArchive(String path, String archiveName) {

            }
        };
        IFernflowerLogger fernflowLogger = new IFernflowerLogger() {

            @Override
            public void writeMessage(String message, Severity severity) {
                switch (severity) {
                    case INFO:
                        log.info(message);
                        break;
                    case WARN:
                        log.warn(message);
                        break;
                    case TRACE:
                        log.trace(message);
                        break;
                    case ERROR:
                        log.error(message);
                        break;
                }
            }

            @Override
            public void writeMessage(String message, Severity severity, Throwable t) {
                if (t != null) {
                    log.error(message, t);
                } else {
                    writeMessage(message, severity);
                }
            }
        };
        //TODO options
        Map<String, Object> options = new HashMap<>();
        fernflower = new Fernflower(bytecodeInput, sourcecodeOutput, options, fernflowLogger);
    }

    @Override
    public void release() {

    }

    private String getAbsolutePath(String path) {
        return new File(root, path).getAbsolutePath();
    }

    public void decompile(String namespace, String classname, byte[] bytecode) {
        this.bytecode = bytecode;
        try {
            fernflower.decompileContext();
        } finally {
            fernflower.clearContext();
        }
    }

    public String findSource(String namespace, String classname) {

        return null;
    }
}
