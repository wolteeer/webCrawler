package org.humanityx.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that a class/implementation is probably thread safe, possibly apart from an initialization/constructor phase.
 * That's how strict you can get it for now...
 *
 * @author Arvid
 * @version 3-6-2015 - 20:38
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //can use in method only.
public @interface ProbablyThreadSafe {
    //should ignore this test?
    public boolean enabled() default true;
}

