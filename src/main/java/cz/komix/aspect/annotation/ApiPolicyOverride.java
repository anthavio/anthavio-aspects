package cz.komix.aspect.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cz.komix.aspect.ApiPolicyAspect;

/**
 * Znackovaci anotace pro {@link ApiPolicyAspect}
 *
 * @author vanek
 */
@Target( { ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiPolicyOverride {

}
