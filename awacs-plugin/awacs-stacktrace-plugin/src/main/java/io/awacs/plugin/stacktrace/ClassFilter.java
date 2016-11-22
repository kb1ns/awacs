package io.awacs.plugin.stacktrace;

/**
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
