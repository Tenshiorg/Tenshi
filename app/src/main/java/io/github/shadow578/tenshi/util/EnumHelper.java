package io.github.shadow578.tenshi.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.*;


/**
 * helper class for working with enums
 */
public class EnumHelper {
    /**
     * parse a enum value from a string, optionally ignoring case
     *
     * @param enumType        the enum type to parse
     * @param str             the string to parse into the enum
     * @param def             the default value, in case the type cannot be found
     * @param caseInsensitive should we ignore case?
     * @param <T>             the enum type
     * @return the parsed enum value, or def if not found
     */
    @NonNull
    public static <T extends Enum<T>> T parseEnum(@NonNull Class<T> enumType, @Nullable String str, @NonNull T def, boolean caseInsensitive) {
        T[] enumValues = enumType.getEnumConstants();
        if (isNull(enumValues))
            return def;

        for (T e : enumValues)
            if (caseInsensitive) {
                if (e.name().equalsIgnoreCase(str))
                    return e;
            } else {
                if (e.name().equals(str))
                    return e;
            }

        return def;
    }

    /**
     * get all values of a enum, honoring @SerializedName attribute
     *
     * @param enumType the enum type
     * @param <T>      the enum type
     * @return a array of all enum values, as returned by EnumHelper::valueOf
     */
    @SuppressWarnings({"unused", "RedundantSuppression"})
    @NonNull
    public static <T extends Enum<T>> String[] getValues(@NonNull Class<T> enumType) {
        ArrayList<String> values = new ArrayList<>();
        T[] enumValues = enumType.getEnumConstants();
        if (isNull(enumValues))
            return new String[0];

        for (T e : enumValues) {
            String val = valueOf(e);
            if (!nullOrWhitespace(val))
                values.add(val);
        }

        return values.toArray(new String[0]);
    }

    /**
     * get the value of a enum, honoring @SerializedName attribute
     *
     * @param e   the enum value
     * @return the name of the enum value (either from @SerializedName, or enum::name())
     */
    @Nullable
    public static String valueOf(@Nullable Enum<?> e) {
        if(isNull(e))
            return null;

        try {
            // allow usage of @SerializedName attribute on enum values
            final SerializedName sn = e.getClass().getField(e.name()).getAnnotation(SerializedName.class);
            if (notNull(sn))
                return sn.value();
        } catch (NoSuchFieldException ignored) {

        }

        // fallback to just enum name
        return e.name();
    }
}
