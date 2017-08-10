package io.awacs.core.trace;

import io.awacs.core.Plugin;

import java.util.regex.Pattern;

/**
 * TODO under development
 * Created by pixyonly on 4/26/17.
 */
public abstract class TracingPlugin implements Plugin {

    protected abstract TracingPlugin addClassFilter(ClassFilter filter);

    protected abstract TracingPlugin addMethodFilter(MethodFilter filter);

    interface ClassFilter<P> {

        boolean doFilter(P p);
    }

    interface MethodFilter<P> {

        //TODO parameter
        boolean doFilter(P p);
    }

    public static abstract class NamePatternClassFilter implements ClassFilter<String> {

        private Pattern pattern;

        public abstract Pattern getPattern();

        @Override
        public boolean doFilter(String className) {
            if (this.pattern == null) {
                this.pattern = getPattern();
            }
            return this.pattern.matcher(className).find();
        }
    }

    public static abstract class InheritedClassFilter implements ClassFilter<String> {

        @Override
        public boolean doFilter(String s) {
            return false;
        }
    }

    public static class AnnotatedClassFilter implements ClassFilter {

        @Override
        public boolean doFilter(Object classNode) {
            return false;
        }
    }
}
