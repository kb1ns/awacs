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
import io.awacs.component.org.jetbrains.java.decompiler.code.CodeConstants;
import io.awacs.component.org.jetbrains.java.decompiler.struct.consts.LinkConstant;
import io.awacs.component.org.jetbrains.java.decompiler.struct.consts.PooledConstant;
import io.awacs.component.org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import io.awacs.component.org.jetbrains.java.decompiler.util.DataInputFullStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by pixyonly on 30/09/2017.
 */
public class FernflowerComponent implements Configurable, Releasable {

    private static Logger log = LoggerFactory.getLogger(FernflowerComponent.class);

    private static int POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private List<Decompiler> decompilerPool;

    //TODO
    private String root = "/tmp/";

    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void init(Configuration configuration) {
        decompilerPool = new ArrayList<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            decompilerPool.add(new Decompiler());
        }
    }

    @Override
    public void release() {
        new File(root).deleteOnExit();
    }

    private String decompile(String namespace, String classname) {
        int index = counter.intValue() % POOL_SIZE;
        counter.getAndUpdate(operand -> operand >= POOL_SIZE ? 0 : operand + 1);
        Decompiler d = decompilerPool.get(index < 0 ? 0 : index);
        if (d != null) {
            File dir = new File(root, namespace);
            File f = new File(dir, classname + ".class");
            if (f.exists() && !f.canRead() && f.isDirectory()) {
                synchronized (d) {
                    return d.decompile(dir, f);
                }
            }
        }
        return null;
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
        int size = stream.readUnsignedShort();
        List<PooledConstant> pool = new ArrayList<>(size);
        BitSet[] nextPass = {new BitSet(size), new BitSet(size), new BitSet(size)};

        // first dummy constant
        pool.add(null);

        // first pass: read the elements
        for (int i = 1; i < size; i++) {
            byte tag = (byte) stream.readUnsignedByte();

            switch (tag) {
                case CodeConstants.CONSTANT_Utf8:
                    pool.add(new PrimitiveConstant(CodeConstants.CONSTANT_Utf8, stream.readUTF()));
                    break;

                case CodeConstants.CONSTANT_Integer:
                    pool.add(new PrimitiveConstant(CodeConstants.CONSTANT_Integer, Integer.valueOf(stream.readInt())));
                    break;

                case CodeConstants.CONSTANT_Float:
                    pool.add(new PrimitiveConstant(CodeConstants.CONSTANT_Float, stream.readFloat()));
                    break;

                case CodeConstants.CONSTANT_Long:
                    pool.add(new PrimitiveConstant(CodeConstants.CONSTANT_Long, stream.readLong()));
                    pool.add(null);
                    i++;
                    break;

                case CodeConstants.CONSTANT_Double:
                    pool.add(new PrimitiveConstant(CodeConstants.CONSTANT_Double, stream.readDouble()));
                    pool.add(null);
                    i++;
                    break;

                case CodeConstants.CONSTANT_Class:
                case CodeConstants.CONSTANT_String:
                case CodeConstants.CONSTANT_MethodType:
                    pool.add(new PrimitiveConstant(tag, stream.readUnsignedShort()));
                    nextPass[0].set(i);
                    break;

                case CodeConstants.CONSTANT_NameAndType:
                    pool.add(new LinkConstant(tag, stream.readUnsignedShort(), stream.readUnsignedShort()));
                    nextPass[0].set(i);
                    break;

                case CodeConstants.CONSTANT_Fieldref:
                case CodeConstants.CONSTANT_Methodref:
                case CodeConstants.CONSTANT_InterfaceMethodref:
                case CodeConstants.CONSTANT_InvokeDynamic:
                    pool.add(new LinkConstant(tag, stream.readUnsignedShort(), stream.readUnsignedShort()));
                    nextPass[1].set(i);
                    break;

                case CodeConstants.CONSTANT_MethodHandle:
                    pool.add(new LinkConstant(tag, stream.readUnsignedByte(), stream.readUnsignedShort()));
                    nextPass[2].set(i);
                    break;
            }
        }
        stream.discard(2);
        int thisClassIdx = stream.readUnsignedShort();
        int utf8Idx = ((PrimitiveConstant) pool.get(thisClassIdx)).index;
        return ((PrimitiveConstant) pool.get(utf8Idx)).getString().replaceAll("/", ".");
    }

    public void record(String namespace, byte[] bytecode) {
        File f = new File(root, namespace);
        if (f.exists()) {
            if (!f.isDirectory()) {
                log.error("{} exists, but it is not directory.");
                return;
            }
        } else if (!f.mkdir()) {
            log.error("Cannot mkdir in {}", f.getAbsolutePath());
            return;
        }
        FileOutputStream w = null;
        try {
            String classname = extractQualifiedName(bytecode);
            File l = new File(f, classname + ".class");
            w = new FileOutputStream(l);
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
//        sourceRecord.putIfAbsent(String.format("%s-%s", namespace, classname),
//                l.getAbsolutePath());
        File pri = new File(root, namespace);
        File f = new File(pri, classname);
        if (!f.exists()) {
            log.warn("Can't find classfile {} in {}", f, pri);
            return null;
        } else {
            return decompile(namespace, classname);
        }
    }
}
