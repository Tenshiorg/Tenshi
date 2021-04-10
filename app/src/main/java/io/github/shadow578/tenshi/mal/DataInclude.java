package io.github.shadow578.tenshi.mal;

import androidx.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * include data annotation, to manually add fields to {@link MalApiHelper#getQueryableFields(Class)} for fields whose type does not have {@link Data} annotation
 */
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DataInclude {
    /**
     * @return field names to include, added to {}
     */
    @NonNull
    String[] includeFields();
}
