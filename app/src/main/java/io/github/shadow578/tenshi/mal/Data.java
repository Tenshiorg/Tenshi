package io.github.shadow578.tenshi.mal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * data annotation, to indicate to {@link MalApiHelper#getQueryableFields(Class)} that this classes fields can/should be added to query params
 */
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Data {
}
