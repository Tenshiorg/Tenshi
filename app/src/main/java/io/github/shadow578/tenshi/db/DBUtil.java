package io.github.shadow578.tenshi.db;

import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * database util functions
 */
public final class DBUtil {
    /**
     * merge two objects fields using reflection.
     * replaces null value fields in newObj with the value of that field in oldObj
     * <p>
     * assuming the following values:
     * oldObj: {name: null, desc: "bar"}
     * newObj: {name: "foo", desc: null}
     * <p>
     * results in the "sum" of both objects: {name: "foo", desc: "bar"}
     *
     * @param type   the type of the two objects to merge
     * @param oldObj the old object
     * @param newObj the new object. after the function, this is the merged object
     * @param <T>    the type
     * @implNote This function uses reflection, and thus is quite slow.
     * The fastest way of doing this would be to use SQLs' ifnull or coalesce (about 35% faster), but that would involve manually writing a expression for EVERY field.
     * That is a lot of extra code which i'm not willing to write...
     * Besides, as long as this function isn't called too often, it doesn't really matter anyway
     */
    public static <T> void merge(@NonNull Class<T> type, @NonNull T oldObj, @NonNull T newObj) {
        // loop through each field that is accessible in the target type
        for (Field f : type.getFields()) {
            // get field modifiers
            final int mod = f.getModifiers();

            // check this field is not status and not final
            if (!Modifier.isStatic(mod)
                    && !Modifier.isFinal(mod)) {
                // try to merge
                // get values of both the old and new object
                // if the new object has a null value, set the value of the new object to that of the old object
                // otherwise, keep the new value
                try {
                    final Object oldVal = f.get(oldObj);
                    final Object newVal = f.get(newObj);

                    if (newVal == null)
                        f.set(newObj, oldVal);
                } catch (IllegalAccessException e) {
                    Log.e("Tenshi", "IllegalAccess in merge: " + e.toString());
                    e.printStackTrace();
                }
            }
        }
    }
}
