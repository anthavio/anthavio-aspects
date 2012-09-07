/**
 * 
 */
package cz.komix.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cz.komix.aspect.LogAspect;

/**
 * @author vanek
 * 
 * Znackovaci anotace pro {@link LogAspect}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR })
public @interface Logged {

	Mode mode() default Mode.AROUND;

	/**
	 * @return parameter types that never should be logged as value
	 */
	Class<?>[] notTypes() default {};

	/**
	 * @return parameter indexes that never should be logged as value
	 */
	int[] notParIdxs() default {};

	/**
	 * @return maximal length of string representing parameter/return value
	 */
	int maxLength() default Integer.MAX_VALUE;

	/**
	 * @return true if return type/value should be logged
	 */
	boolean logRetVal() default true;

	/**
	 * @return true if execution time should be logged
	 */
	boolean logTime() default true;

	/**
	 * @return true if parameter value should be always logged regardless log level
	 */
	boolean forceValues() default false;

	/**
	 * @return true if parameter only type should be always logged regardless log level 
	 */
	boolean forceTypes() default false;

	/**
	 * @return true compute execution time statisticts for method
	 */
	boolean statistics() default false;

	/**
	 * @return print stackTrace when exception happens
	 */
	boolean stackTrace() default false;

	enum Mode {
		ENTER, EXIT, AROUND;
	}
}
