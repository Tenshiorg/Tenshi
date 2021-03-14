package io.github.shadow578.tenshi.lang;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * expands the java language by some useful functions
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class LanguageUtils {
    //region Collections

    /**
     * check if a collection is null or empty
     *
     * @param cx  the collectio to check
     * @param <T> the collection type
     * @return is the collection either null or empty?
     */
    public static <T> boolean nullOrEmpty(@Nullable Collection<T> cx) {
        return cx == null || cx.isEmpty();
    }

    /**
     * create a list of the given values
     *
     * @param x   the list items
     * @param <T> the type of the list
     * @return the list
     */
    @SafeVarargs
    @NonNull
    public static <T> ArrayList<T> listOf(@NonNull T... x) {
        return new ArrayList<>(Arrays.asList(x));
    }


    /**
     * create a list of the given list values
     *
     * @param cx  the list items
     * @param <T> the type of the list
     * @return the list
     */
    @SafeVarargs
    @NonNull
    public static <T> ArrayList<T> listOf(@Nullable Collection<T>... cx) {
        ArrayList<T> ret = new ArrayList<>();
        if (notNull(cx))
            for (Collection<T> c : cx)
                if (notNull(c))
                    ret.addAll(c);
        return ret;
    }
    //endregion

    //region Nullables

    /**
     * check t is null
     *
     * @param t   the value to check
     * @param <T> the type of t
     * @return is t null?
     */
    public static <T> boolean isNull(@Nullable T t) {
        return t == null;
    }

    /**
     * check t is not null
     *
     * @param t   the value to check
     * @param <T> the type of t
     * @return is t not null?
     */
    public static <T> boolean notNull(@Nullable T t) {
        return t != null;
    }

    /**
     * run a function if t is not null
     *
     * @param t    the value to pass to the function
     * @param func the function to run
     * @param <T>  the type of t
     * @param <R>  the return type of the function
     * @return the value returned by func, or null if t is null
     */
    @Nullable
    public static <T, R> R withRet(@Nullable T t, @NonNull Function<R, T> func) {
        if (t != null)
            return func.invoke(t);
        return null;
    }

    /**
     * run a function if t is not null
     *
     * @param t    the value to pass to the function
     * @param def  the default value to use if t is null
     * @param func the function to run
     * @param <T>  the type of t
     * @param <R>  the return type of the function
     * @return the value returned by func, or def if t is null
     */
    @NonNull
    public static <T, R> R withRet(@Nullable T t, @NonNull R def, @NonNull Function<R, T> func) {
        if (t != null)
            return func.invoke(t);
        return def;
    }

    /**
     * run a function if t is not null
     *
     * @param t    the value to pass to the function
     * @param func the function to run
     * @param <T>  the type of t
     */
    public static <T> void with(@Nullable T t, @NonNull Consumer<T> func) {
        if (t != null)
            func.invoke(t);
    }

    /**
     * run a function with t, or def if t is null
     *
     * @param t    the value to pass to the function
     * @param func the function to run
     * @param def  the default value to use in case t is null
     * @param <T>  the type of t
     */
    public static <T> void with(@Nullable T t, @NonNull T def, @NonNull Consumer<T> func) {
        if (t != null)
            func.invoke(t);
        else
            func.invoke(def);
    }

    /**
     * run a function with t.toString(), or def if t or t.toString() is null
     *
     * @param t    the value to pass to the function
     * @param func the function to run
     * @param def  the default value to use in case t is null
     * @param <T>  the type of t
     */
    public static <T> void withStr(@Nullable T t, @NonNull String def, @NonNull Consumer<String> func) {
        if (t != null && !nullOrEmpty(t.toString()))
            func.invoke(t.toString());
        else
            func.invoke(def);
    }

    /**
     * return t if t is not null, else return def
     * eg. elvis operator ?:
     *
     * @param t   the value to return if not null
     * @param def the value to return if t is null
     * @param <T> the type of t, def and return
     * @return t if t is not null, else def (== t ?: def)
     */
    @NonNull
    public static <T> T elvis(@Nullable T t, @NonNull T def) {
        return t != null ? t : def;
    }

    /**
     * return t if t is not null, else return def
     * eg. elvis operator ?: with added emptyness check for strings
     *
     * @param t   the value to return if not null or empty
     * @param def the value to return if t is null or empty
     * @return t if t is not null, else def (== t ?: def)
     */
    @NonNull
    public static String elvisEmpty(@Nullable String t, @NonNull String def) {
        return !nullOrEmpty(t) ? t : def;
    }

    /**
     * dereference a variable null- safe.
     * Warning: this uses a try/catch under the hood, so all NullPointerExceptions thrown by func will be swallowed.
     *
     * @param t    the object to do a dereference on
     * @param func the dereference function, return value is returned
     * @param <T>  the type of t
     * @param <R>  the type to return
     * @return the dereferenced value, or null if failed
     */
    @Nullable
    public static <T, R> R nullSafe(@Nullable T t, @NonNull Function<R, T> func) {
        try {
            return func.invoke(t);
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * dereference a variable null- safe, with default
     * Warning: this uses a try/catch under the hood, so all NullPointerExceptions thrown by func will be swallowed.
     *
     * @param t    the object to do a dereference on
     * @param func the dereference function, return value is returned
     * @param <T>  the type of t
     * @param <R>  the type to return
     * @return the dereferenced value, or def if failed
     */
    @NonNull
    public static <T, R> R nullSafe(@Nullable T t, @NonNull Function<R, T> func, @NonNull R def) {
        try {
            R ret = func.invoke(t);
            return ret != null ? ret : def;
        } catch (NullPointerException e) {
            return def;
        }
    }
    //endregion

    //region Strings

    /**
     * convert the object into a string, using String.valueOf
     *
     * @param o the object to convert
     * @return the string
     */
    @Nullable
    public static String str(@Nullable Object o) {
        if (isNull(o))
            return null;

        return String.valueOf(o);
    }

    /**
     * concatenate multiple strings
     *
     * @param sx the strings to concatenate
     * @return the concatenated string
     */
    @NonNull
    public static String concat(@NonNull String... sx) {
        StringBuilder sb = new StringBuilder();
        for (String s : sx)
            sb.append(s);
        return sb.toString();
    }

    /**
     * join multiple strings with delimiter
     *
     * @param delimiter the delimiter to put between the strings
     * @param sx        the strings to join
     * @return the joined string
     */
    @NonNull
    public static String join(@NonNull String delimiter, @NonNull String... sx) {
        // use concat if delimiter is empty
        if (nullOrEmpty(delimiter))
            return concat(sx);

        // join strings
        StringBuilder sb = new StringBuilder();
        for (String s : sx)
            sb.append(s).append(delimiter);

        //remove last delimiter
        sb.setLength(sb.length() - delimiter.length());
        return sb.toString();
    }

    /**
     * join multiple fields of a object, with delimiter
     *
     * @param delimiter the delimiter to put between the strings
     * @param items     the list of objects to join
     * @param prop      the field selector for objects
     * @param <T>       the type of the objects to join
     * @return the joined string
     */
    @NonNull
    public static <T> String join(@NonNull String delimiter, @Nullable Collection<T> items, @NonNull Function<String, T> prop) {
        StringBuilder sb = new StringBuilder();
        foreach(items, p -> sb.append(prop.invoke(p)).append(delimiter));

        //remove last delimiter
        if (sb.length() > delimiter.length())
            sb.setLength(sb.length() - delimiter.length());
        return sb.toString();
    }

    /**
     * format a string using String.format
     *
     * @param s      the string to format
     * @param format formatting parameters
     * @return the formatted string
     */
    @NonNull
    public static String fmt(String s, Object... format) {
        return String.format(s, format);
    }

    /**
     * format a string resource using String.format
     *
     * @param ctx    the context to get the string in
     * @param sRes   the string to format
     * @param format formatting parameters
     * @return the formatted string
     */
    @NonNull
    public static String fmt(@NonNull Context ctx, @StringRes int sRes, Object... format) {
        return String.format(ctx.getString(sRes), format);
    }

    /**
     * wrapper for NumberFormat.getInstance().format()
     *
     * @param v the value to format
     * @return the formatted value
     */
    public static String fmt(@Nullable Integer v) {
        if (notNull(v))
            return NumberFormat.getInstance().format(v);

        return "-";
    }

    /**
     * wrapper for NumberFormat.getInstance().format()
     *
     * @param v the value to format
     * @return the formatted value
     */
    public static String fmt(long v) {
        return NumberFormat.getInstance().format(v);
    }

    /**
     * wrapper for NumberFormat.getInstance().format()
     *
     * @param v the value to format
     * @return the formatted value
     */
    public static String fmt(double v) {
        return NumberFormat.getInstance().format(v);
    }

    /**
     * check if a string is null or empty
     *
     * @param s the string to check
     * @return is s null OR empty
     */
    public static boolean nullOrEmpty(@Nullable String s) {
        return s == null || s.isEmpty();
    }

    /**
     * check if a string is null, empty or only whitespaces
     *
     * @param s the string to check
     * @return is s null, empty or only whitespace
     */
    public static boolean nullOrWhitespace(@Nullable String s) {
        return nullOrEmpty(s) || s.trim().isEmpty();
    }
    //endregion

    //region loops

    /**
     * run a foreach loop if the list is not null or empty
     *
     * @param list the list to loop over
     * @param func the loop body
     * @param <T>  the type of the list
     */
    public static <T> void foreach(@Nullable Collection<T> list, @NonNull Consumer<T> func) {
        if (isNull(list) || list.isEmpty())
            return;
        for (T t : list)
            func.invoke(t);
    }

    /**
     * run a foreach loop if the list is not null or empty
     *
     * @param list the list to loop over
     * @param func the loop body
     * @param <T>  the type of the list
     */
    public static <T> void foreach(@Nullable Collection<T> list, @NonNull BiConsumer<T, Integer> func) {
        if (isNull(list) || list.isEmpty())
            return;

        int i = 0;
        for (T t : list)
            func.invoke(t, i++);
    }

    /**
     * run a foreach loop if the list is not null or empty and return teh results as a collection
     *
     * @param list the list to loop over
     * @param func the loop body
     * @param <T>  the type of the list
     * @param <R>  the return type of the function and type of the list
     * @return a collection of all values the function returned. may have null values, depending on your function
     */
    @Nullable
    public static <T, R> Collection<R> collect(@Nullable Collection<T> list, @NonNull Function<R, T> func) {
        if (isNull(list) || list.isEmpty())
            return null;

        ArrayList<R> results = new ArrayList<>();
        int i = 0;
        for (T t : list)
            results.add(func.invoke(t));

        return results;
    }

    /**
     * run a foreach loop if the list is not null or empty and return teh results as a collection
     *
     * @param list the list to loop over
     * @param func the loop body
     * @param <T>  the type of the list
     * @param <R>  the return type of the function and type of the list
     * @return a collection of all values the function returned. may have null values, depending on your function
     */
    @Nullable
    public static <T, R> Collection<R> collect(@Nullable T[] list, @NonNull Function<R, T> func) {
        if (isNull(list) || list.length <= 0)
            return null;

        ArrayList<R> results = new ArrayList<>();
        int i = 0;
        for (T t : list)
            results.add(func.invoke(t));

        return results;
    }

    /**
     * repeat a function call count times and return the results as a collection
     *
     * @param start the start value (inclusive)
     * @param end   the end value (inclusive)
     * @param func  the function to repeat. takes the current iteration as parameter
     * @param <T>   the return type of the function to repeat
     * @return a collection of all values the function returned. may have null values, depending on your function
     */
    @NonNull
    public static <T> Collection<T> repeat(int start, int end, @NonNull Function<T, Integer> func) {
        if (start > end)
            throw new IllegalArgumentException("start has to be smaller than end!");

        ArrayList<T> results = new ArrayList<>();
        for (int i = start; i <= end; i++)
            results.add(func.invoke(i));
        return results;
    }
    //endregion

    //region Cast & Parse

    /**
     * safely cast the object
     *
     * @param o   the object to cast
     * @param <T> the type to cast to
     * @return the casted object, or null if the cast failed
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T cast(@Nullable Object o) {
        // if o is null, we cannot cast
        if (isNull(o))
            return null;

        // do cast
        try {
            return (T) o;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * safely cast the object, with default in case the cast fails
     *
     * @param o   the object to cast
     * @param <T> the type to cast to
     * @return the casted object, or def if the cast failed
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T cast(@Nullable Object o, @NonNull T def) {
        // if o is null, we cannot cast
        if (isNull(o))
            return def;

        // do cast
        try {
            return (T) o;
        } catch (ClassCastException e) {
            return def;
        }
    }
    //endregion

    //region async
    /**
     * executor to run background stuff in
     */
    private static final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    /**
     * handler in the main thread to post results
     */
    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * run a action async and post the result to a callback in the main thread.
     * @param action the action to execute
     * @param callback the callback to post the result to. Result may be null
     * @param <Rt> action return type
     */
    public static <Rt> void async(@NonNull Action<Rt> action, @NonNull Consumer<Rt> callback)
    {
        backgroundExecutor.execute(() -> {
            final Rt r = action.invoke();
            mainThreadHandler.post(() -> {
                callback.invoke(r);
            });
        });
    }
    //endregion
}
