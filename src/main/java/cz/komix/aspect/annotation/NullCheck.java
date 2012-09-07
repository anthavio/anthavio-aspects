package cz.komix.aspect.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import cz.komix.aspect.NullCheckAspect;

/**
 * Zakladem je javax.validation.NotNull. Znackovaci anotace pro {@link NullCheckAspect}
 *
 * @author vanek
 */
@Retention(RUNTIME)
@Target( { PARAMETER, METHOD, CONSTRUCTOR })
@Documented
public @interface NullCheck {
	String message() default "{cz.komix.validation.constraints.NotNull.message}";

	Class<?>[] groups() default {};
	/*
		@Target( { METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
		@Retention(RUNTIME)
		@Documented
		@interface List {
			NullCheck[] value();
		}
	*/
}
