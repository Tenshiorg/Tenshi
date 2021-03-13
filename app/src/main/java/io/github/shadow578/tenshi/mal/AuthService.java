package io.github.shadow578.tenshi.mal;

import io.github.shadow578.tenshi.mal.model.Token;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * MAL authorization api endpoints
 */
public interface AuthService {

    /**
     * PKCE code to token exchange endpoint
     * @param clientID your MAL client id
     * @param code the code received from the PKCE OAUTH flow
     * @param verifier verifier code
     * @param grantType grant type, always "authorization_code"
     * @return the token from MAL
     */
    @FormUrlEncoded
    @POST("/v1/oauth2/token")
    Call<Token> getAccessToken(@Field("client_id") String clientID,
                               @Field("code") String code,
                               @Field("code_verifier") String verifier,
                               @Field("grant_type") String grantType);

    /**
     * Refresh token endpoint
     * @param clientID your MAL client id
     * @param grantType grant type, always "refresh_token"
     * @param refreshToken the refresh token
     * @return the new token from MAL
     */
    @FormUrlEncoded
    @POST("/v1/oauth2/token")
    Call<Token> refreshAccessToken(@Field("client_id") String clientID,
                                   @Field("grant_type") String grantType,
                                   @Field("refresh_token") String refreshToken);
}
