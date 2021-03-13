package io.github.shadow578.tenshi.util.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.github.shadow578.tenshi.util.EnumHelper;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.internal.EverythingIsNonNull;

/**
 * Converter Factory for Enums for Retrofit (@Query and @Path support for Enums).
 * Uses {@link EnumHelper} under the hood, so is compatible with @SerializedName attribute on enum values.
 */
public class RetrofitEnumConverterFactory extends Converter.Factory {

    @Override
    @EverythingIsNonNull
    public Converter<Enum<?>, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (type instanceof Class && ((Class<?>) type).isEnum())
            return EnumHelper::valueOf;
        else
            return null;
    }
}
