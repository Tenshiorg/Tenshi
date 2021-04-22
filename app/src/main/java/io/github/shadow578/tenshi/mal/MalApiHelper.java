package io.github.shadow578.tenshi.mal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import io.github.shadow578.tenshi.BuildConfig;
import io.github.shadow578.tenshi.mal.model.ErrorResponse;
import io.github.shadow578.tenshi.mal.model.Token;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.listOf;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrWhitespace;

/**
 * helper class for everything MAL api
 */
public class MalApiHelper {

    /**
     * refresh the OAUTH token using the given refresh token
     *
     * @param refreshToken the refresh token to use
     * @param callback     the callback for the call
     */
    public static void doRefreshToken(@NonNull String refreshToken, @NonNull Callback<Token> callback) {
        final AuthService authService = MalApiHelper.createService(AuthService.class);
        final Call<Token> refreshCall = authService.refreshAccessToken(BuildConfig.MAL_CLIENT_ID, "refresh_token", refreshToken);
        refreshCall.enqueue(callback);
    }

    /**
     * get the error response object from a failed response
     *
     * @param response the failed response
     * @return the error response. if parsing failed, null is returned
     */
    @Nullable
    public static ErrorResponse getError(@NonNull okhttp3.Response response) {
        try {
            // get raw response json
            // use peek to not disturb any possible call chain. However, limit to just 64k as a error response shouldn't be any bigger anyways
            final String json = response.peekBody(65536).string();

            // deserialize using Gson
            return new Gson().fromJson(json, ErrorResponse.class);
        } catch (IOException | JsonSyntaxException ignored) {
            return null;
        }
    }

    /**
     * create a API service instance with auth token.
     * On debug builds, a logging interceptor is added
     *
     * @param serviceClass the service to create
     * @param <T>          service type
     * @return the created service instance
     */
    @NonNull
    public static <T> T createService(@NonNull Class<T> serviceClass) {
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        final OkHttpClient.Builder cl = new OkHttpClient.Builder()
                .protocols(listOf(Protocol.HTTP_1_1))
                .addInterceptor(loggingInterceptor);

        final Retrofit rf = new Retrofit.Builder()
                .baseUrl(Urls.BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .client(cl.build())
                .build();
        return rf.create(serviceClass);
    }

    /**
     * get comma- separated string of all queryable fields in a type and its fields
     * Type has to be annotated with @Data
     *
     * @param type the data type to get queryable fields of
     * @param <T>  type
     * @return a query string of all fields (eg. foo,bar,nested{foo,bar},...)
     */
    @NonNull
    public static <T> String getQueryableFields(@NonNull Class<T> type) {
        // check if this is a data class
        if (isNull(type.getAnnotation(Data.class)))
            return "";

        // get all fields
        StringBuilder query = new StringBuilder();
        for (Field field : type.getFields()) {
            // ignore transient, static and private fields
            if (Modifier.isTransient(field.getModifiers())
                    || Modifier.isStatic(field.getModifiers())
                    || Modifier.isPrivate(field.getModifiers()))
                continue;

            // fallback to plain field name
            String fieldName = field.getName();

            // use @SerializedName if found
            final SerializedName sn = field.getAnnotation(SerializedName.class);
            if (notNull(sn) && !nullOrWhitespace(sn.value()))
                fieldName = sn.value();

            // append to query string
            query.append(fieldName);

            // setup sub- fields to query for this field
            final StringBuilder subFields = new StringBuilder();

            // if the fields type has @Data annotation, get fields from that and add them
            if (notNull(field.getType().getAnnotation(Data.class))) {
                String sf = getQueryableFields(field.getType());
                if (!nullOrEmpty(sf))
                    subFields.append(sf);
            }

            // if the field has @DataInclude annotation, add that too
            final DataInclude di = field.getAnnotation(DataInclude.class);
            if (notNull(di))
                for (String include : di.includeFields())
                    subFields.append(include);

            // add subfields to query
            if (subFields.length() > 0)
                query.append("{").append(subFields.toString()).append("}");

            // comma for the next one
            query.append(",");
        }

        // remove last comma
        if (query.length() > 0)
            query.setLength(query.length() - 1);
        return query.toString();
    }
}
