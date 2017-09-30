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

import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.Releasable;
import io.awacs.component.org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import io.awacs.component.org.jetbrains.java.decompiler.util.DataInputFullStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by pixyonly on 30/09/2017.
 */
public class FernflowerComponent implements Configurable, Releasable {

    private static Logger log = LoggerFactory.getLogger(FernflowerComponent.class);

    private ConcurrentMap<String, Decompiler> decompilerPool;

    //TODO
    private String root = "/tmp/";

    @Override
    public void init(Configuration configuration) {
        decompilerPool = new ConcurrentHashMap<>();
    }

    @Override
    public void release() {

    }

    private void decompile(String namespace, String classname) {
        Decompiler d = decompilerPool.get(namespace);
        if (d != null) {
            //TODO check file
            File f = new File(root + namespace, classname + ".class");
            synchronized (d) {
                d.decompile(f);
            }
        }
    }

    private String extractQualifiedName(final byte[] bytecode) throws IOException {
//        u4 magic;
//        u2 minor_version;
//        u2 major_version;
//        u2 constant_pool_count;
//        cp_info constant_pool[constant_pool_count-1];
//        u2 access_flags;
//        u2 this_class;
        DataInputFullStream stream = new DataInputFullStream(bytecode);
        stream.discard(8);
        ConstantPool pool = new ConstantPool(stream);
        stream.discard(2);
        int thisClassIdx = stream.readUnsignedShort();
        return pool.getPrimitiveConstant(thisClassIdx).getString().replaceAll("/", ".");
    }

    public void record(String namespace, byte[] bytecode) {
        File f = new File(root + namespace);
        if (!decompilerPool.containsKey(namespace)) {
            if (f.exists()) {
                if (!f.isDirectory()) {
                    log.error("{} exists, but it is not directory.");
                    return;
                }
            } else if (!f.mkdir()) {
                log.error("Cannot mkdir in {}", root + namespace);
                return;
            }
            decompilerPool.putIfAbsent(namespace, new Decompiler(f));
        }
        FileOutputStream w = null;
        try {
            String classname = extractQualifiedName(bytecode);
            w = new FileOutputStream(new File(f, classname + ".class"));
            w.write(bytecode);
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (w != null) {
                    w.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public String findSource(String namespace, String classname) {

        return null;
    }
}
