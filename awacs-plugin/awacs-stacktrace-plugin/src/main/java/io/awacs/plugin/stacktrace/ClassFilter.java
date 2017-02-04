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

package io.awacs.plugin.stacktrace;

/**
 *
 * Created by pixyonly on 16/11/22.
 */
public class ClassFilter {

    private String[] filterClassPrefix;

    ClassFilter(String[] filterClassPrefix) {
        this.filterClassPrefix = filterClassPrefix;
    }

    public boolean doFilter(String className) {
        if (className == null) {
            return false;
        } else if (className.startsWith("io/awacs/plugin/")) {
            return false;
        } else if (filterClassPrefix == null || filterClassPrefix.length == 0) {
            return !(className.startsWith("java") ||
                    className.startsWith("sun") ||
                    className.startsWith("jdk") ||
                    className.startsWith("com/sun/") ||
                    className.startsWith("com/intellij/") ||
                    className.startsWith("org/"));
        } else {
            boolean flag = false;
            for (String prefix : filterClassPrefix)
                flag = flag || (className.startsWith(prefix.replaceAll("\\.", "/")));
            return flag;
        }
    }
}
