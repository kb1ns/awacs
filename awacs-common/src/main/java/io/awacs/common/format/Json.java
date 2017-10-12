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

package io.awacs.common.format;


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Copy from jdk com.oracle.javafx.jmx.json.JSONWriter
 * Created by pixyonly on 18/09/2017.
 */
public class Json {

    private final StringBuilder writer = new StringBuilder();
    private Stack<Json.Container> where = new Stack<>();

    private Json() {
    }

    public static Json empty() {
        return new Json();
    }

    public Json startObject() {
        this.writeSeparatorIfNeeded();
        this.writer.append("{");
        this.where.push(new Json.Container(Json.ContainerType.OBJECT));
        return this;
    }

    public Json startObject(String key) {
        if (this.where.peek().type != Json.ContainerType.OBJECT) {
            throw new IllegalStateException();
        } else {
            this.writeSeparatorIfNeeded();
            this.writeEscapedString(key);
            this.writer.append(":{");
            this.where.push(new Json.Container(Json.ContainerType.OBJECT));
            return this;
        }
    }

    public Json endObject() {
        if (this.where.peek().type != Json.ContainerType.OBJECT) {
            throw new IllegalStateException();
        } else {
            this.where.pop();
            this.writer.append("}");
            return this;
        }
    }

    public Json startArray() {
        this.writeSeparatorIfNeeded();
        this.writer.append("[");
        this.where.push(new Json.Container(Json.ContainerType.ARRAY));
        return this;
    }

    public Json startArray(String key) {
        if (this.where.peek().type != Json.ContainerType.OBJECT) {
            throw new IllegalStateException();
        } else {
            this.writeSeparatorIfNeeded();
            this.writeEscapedString(key);
            this.writer.append(":[");
            this.where.push(new Json.Container(Json.ContainerType.ARRAY));
            return this;
        }
    }

    public Json endArray() {
        if (this.where.peek().type != Json.ContainerType.ARRAY) {
            throw new IllegalStateException();
        } else {
            this.writer.append("]");
            this.where.pop();
            return this;
        }
    }

    public Json objectValue(String key, Object value) {
        if (this.where.peek().type != Json.ContainerType.OBJECT) {
            throw new IllegalStateException();
        } else {
            this.writeSeparatorIfNeeded();
            this.writeEscapedString(key);
            this.writer.append(":");
            this.writeValue(value);
            return this;
        }
    }

    public Json arrayValue(Object var1) {
        if (this.where.peek().type != Json.ContainerType.ARRAY) {
            throw new IllegalStateException();
        } else {
            this.writeSeparatorIfNeeded();
            this.writeValue(var1);
            return this;
        }
    }

    public Json object(String key, Map map) {
        this.startObject(key);

        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            this.objectValue((String) entry.getKey(), entry.getValue());
        }

        this.endObject();
        return this;
    }

    public Json array(String key, List array) {
        this.startArray(key);

        for (Object o : array) {
            this.arrayValue(o);
        }

        this.endArray();
        return this;
    }

    private void writeValue(Object v) {
        if (v == null) {
            this.writer.append("null");
        } else {
            Iterator ite;
            if (v instanceof Map) {
                this.startObject();
                ite = ((Map) v).entrySet().iterator();

                while (ite.hasNext()) {
                    Map.Entry var3 = (Map.Entry) ite.next();
                    this.objectValue((String) var3.getKey(), var3.getValue());
                }

                this.endObject();
            } else if (v instanceof List) {
                this.startArray();
                ite = ((List) v).iterator();
                while (ite.hasNext()) {
                    this.arrayValue(ite.next());
                }
                this.endArray();
            } else if (v instanceof Integer ||
                    v instanceof Long ||
                    v instanceof Float ||
                    v instanceof Double ||
                    v instanceof Byte ||
                    v instanceof Short ||
                    v instanceof Boolean) {
                this.writer.append(v.toString());
            } else {
                this.writeEscapedString(v.toString());
            }
        }
    }

    private void writeSeparatorIfNeeded() {
        if (!this.where.empty()) {
            if (this.where.peek().first) {
                this.where.peek().first = false;
            } else {
                this.writer.append(",");
            }
        }
    }

    private void writeEscapedString(String var1) {
        this.writer.append("\"");
        printEscapedString(this.writer, var1);
        this.writer.append("\"");
    }

    @Override
    public String toString() {
        if (writer.length() == 0) {
            return "{}";
        }
        return writer.toString();
    }

    private static class Container {
        Json.ContainerType type;
        boolean first;

        Container(Json.ContainerType var1) {
            this.type = var1;
            this.first = true;
        }
    }

    private enum ContainerType {
        ARRAY,
        OBJECT;

        ContainerType() {
        }
    }

    private static void printEscapedString(StringBuilder var0, String var1) {
        char[] var2 = var1.toCharArray();

        for (char aVar2 : var2) {
            if (aVar2 == 34) {
                var0.append("\\\"");
            } else if (aVar2 == 39) {
                var0.append("\'");
            } else if (aVar2 == 92) {
                var0.append("\\\\");
            } else if (aVar2 == 47) {
                var0.append("\\/");
            } else if (aVar2 == 7) {
                var0.append("\\a");
            } else if (aVar2 == 8) {
                var0.append("\\b");
            } else if (aVar2 == 9) {
                var0.append("\\t");
            } else if (aVar2 == 10) {
                var0.append("\\n");
            } else if (aVar2 == 11) {
                var0.append("\\v");
            } else if (aVar2 == 12) {
                var0.append("\\f");
            } else if (aVar2 == 13) {
                var0.append("\\r");
            } else if (aVar2 == 0) {
                var0.append("\\0");
            } else if (aVar2 > 127 && aVar2 < '\uffff') {
                var0.append("\\u");
                var0.append(String.format("%04X", new Object[]{(int) aVar2}));
            } else {
                var0.append(aVar2);
            }
        }
    }
}
