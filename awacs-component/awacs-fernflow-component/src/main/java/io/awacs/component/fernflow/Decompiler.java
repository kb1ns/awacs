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

package io.awacs.component.fernflow;

import io.awacs.component.org.jetbrains.java.decompiler.main.DecompilerContext;
import io.awacs.component.org.jetbrains.java.decompiler.main.Fernflower;
import io.awacs.component.org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import io.awacs.component.org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import io.awacs.component.org.jetbrains.java.decompiler.main.extern.IResultSaver;
import io.awacs.component.org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by pixyonly on 30/09/2017.
 */
public final class Decompiler {

    private static Logger log = LoggerFactory.getLogger(Decompiler.class);

    private Fernflower fernflower;

    private File root;

    private final Map<String, ZipOutputStream> mapArchiveStreams = new HashMap<>();

    private final Map<String, Set<String>> mapArchiveEntries = new HashMap<>();

    private String source;

    public Decompiler() {
        IBytecodeProvider bytecodeInput = (externalPath, internalPath) -> {
            File file = new File(externalPath);
            if (internalPath == null) {
                return InterpreterUtil.getBytes(file);
            } else {
                try (ZipFile archive = new ZipFile(file)) {
                    ZipEntry entry = archive.getEntry(internalPath);
                    if (entry == null) throw new IOException("Entry not found: " + internalPath);
                    return InterpreterUtil.getBytes(archive, entry);
                }
            }
        };
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
                try {
                    InterpreterUtil.copyFile(new File(source), new File(getAbsolutePath(path), entryName));
                } catch (IOException ex) {
                    DecompilerContext.getLogger().writeMessage("Cannot copy " + source + " to " + entryName, ex);
                }
            }

            @Override
            public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
                File file = new File(getAbsolutePath(path), entryName);
                try (Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF8")) {
                    source = new String(content);
                    out.write(content);
                } catch (IOException ex) {
                    DecompilerContext.getLogger().writeMessage("Cannot write class file " + file, ex);
                }
            }

            @Override
            public void createArchive(String path, String archiveName, Manifest manifest) {
                File file = new File(getAbsolutePath(path), archiveName);
                try {
                    if (!(file.createNewFile() || file.isFile())) {
                        throw new IOException("Cannot create file " + file);
                    }

                    FileOutputStream fileStream = new FileOutputStream(file);
                    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
                    ZipOutputStream zipStream = manifest != null ? new JarOutputStream(fileStream, manifest) : new ZipOutputStream(fileStream);
                    mapArchiveStreams.put(file.getPath(), zipStream);
                } catch (IOException ex) {
                    DecompilerContext.getLogger().writeMessage("Cannot create archive " + file, ex);
                }
            }

            @Override
            public void saveDirEntry(String path, String archiveName, String entryName) {
                saveClassEntry(path, archiveName, null, entryName, null);
            }

            @Override
            public void copyEntry(String source, String path, String archiveName, String entryName) {
                String file = new File(getAbsolutePath(path), archiveName).getPath();

                if (!checkEntry(entryName, file)) {
                    return;
                }

                try (ZipFile srcArchive = new ZipFile(new File(source))) {
                    ZipEntry entry = srcArchive.getEntry(entryName);
                    if (entry != null) {
                        try (InputStream in = srcArchive.getInputStream(entry)) {
                            ZipOutputStream out = mapArchiveStreams.get(file);
                            out.putNextEntry(new ZipEntry(entryName));
                            InterpreterUtil.copyStream(in, out);
                        }
                    }
                } catch (IOException ex) {
                    String message = "Cannot copy entry " + entryName + " from " + source + " to " + file;
                    DecompilerContext.getLogger().writeMessage(message, ex);
                }
            }

            @Override
            public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
                String file = new File(getAbsolutePath(path), archiveName).getPath();

                if (!checkEntry(entryName, file)) {
                    return;
                }

                try {
                    ZipOutputStream out = mapArchiveStreams.get(file);
                    out.putNextEntry(new ZipEntry(entryName));
                    if (content != null) {
                        out.write(content.getBytes("UTF-8"));
                    }
                } catch (IOException ex) {
                    String message = "Cannot write entry " + entryName + " to " + file;
                    DecompilerContext.getLogger().writeMessage(message, ex);
                }
            }

            private boolean checkEntry(String entryName, String file) {
                Set<String> set = mapArchiveEntries.get(file);
                if (set == null) {
                    mapArchiveEntries.put(file, set = new HashSet<>());
                }

                boolean added = set.add(entryName);
                if (!added) {
                    String message = "Zip entry " + entryName + " already exists in " + file;
                    DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.WARN);
                }
                return added;
            }

            @Override
            public void closeArchive(String path, String archiveName) {
                String file = new File(getAbsolutePath(path), archiveName).getPath();
                try {
                    mapArchiveEntries.remove(file);
                    mapArchiveStreams.remove(file).close();
                } catch (IOException ex) {
                    DecompilerContext.getLogger().writeMessage("Cannot close " + file, IFernflowerLogger.Severity.WARN);
                }
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

    private String getAbsolutePath(String path) {
        return new File(root, path).getAbsolutePath();
    }

    public String decompile(File output, File f) {
        this.root = output;
        fernflower.getStructContext().addSpace(f, true);
        try {
            fernflower.decompileContext();
        } catch (Exception e) {
            source = null;
            e.printStackTrace();
        } finally {
            fernflower.clearContext();
        }
        return source;
    }
}
