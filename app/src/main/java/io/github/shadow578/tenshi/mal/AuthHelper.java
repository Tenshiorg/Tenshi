package io.github.shadow578.tenshi.mal;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * OAUTH helper class
 */
public class AuthHelper {

    /**
     * random number generator, for crypto
     */
    private static final SecureRandom rng = new SecureRandom();

    /**
     * generate a random alphanumeric code with the given length.
     * uses a {@link SecureRandom} for generation of the string
     *
     * @param length the length of the code to generate
     * @return the generated code
     */
    @NonNull
    public static String generatePCKECode(int length) {
        final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(CHARS[rng.nextInt(CHARS.length)]);

        return sb.toString();
    }

    /**
     * generate PKCE codes using plain method
     *
     * @return the codes
     */
    @NonNull
    public static PKCECodes generatePlain() {
        String state = generatePCKECode(128);
        String code = generatePCKECode(128);

        return new PKCECodes(state, code, code, "plain");
    }

    /**
     * generate PKCE codes using S256 method.
     * If the system does not have SHA256 algorithm available, falls back to plain method)
     *
     * @return the codes
     */
    @NonNull
    public static PKCECodes generateS256() {
        // generate state and verifier code
        String state = generatePCKECode(128);
        String verifierCode = generatePCKECode(128);

        // generate code challenge using S256 method
        try {
            String codeChallenge = getS256Challenge(verifierCode);
            return new PKCECodes(state, verifierCode, codeChallenge, "S256");
        } catch (NoSuchAlgorithmException e) {
            Log.e("Tenshi", "AuthHelper: SHA256 not installed, fallback to plain. " + e.toString());
            e.printStackTrace();

            // fallback to plain auth
            return generatePlain();
        }
    }

    /**
     * get the challenge code for a verifier code using S256
     *
     * @param verifier the verifier code
     * @return the code_challenge
     */
    @NonNull
    private static String getS256Challenge(@NonNull String verifier) throws NoSuchAlgorithmException {
        // get sha256 of verifier code
        final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        final byte[] verifierHash = sha256.digest(verifier.getBytes(StandardCharsets.UTF_8));

        // convert hash to url- safe base64 string without padding and newline
        return Base64.encodeToString(verifierHash, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }

    /**
     * PKCE code details
     */
    public static final class PKCECodes {

        /**
         * PKCE state, randomized string of lenght 128
         */
        public final String state;

        /**
         * PKCE verification code, for key exchange request
         */
        public final String verifier;

        /**
         * PKCE code_challenge code, using {@see CHALLENGE_METHOD} as code_challenge_method
         */
        public final String challenge;

        /**
         * PKCE code_challenge_method
         */
        public final String challengeMethod;

        private PKCECodes(String state, String verifier, String challenge, String challengeMethod) {
            this.state = state;
            this.verifier = verifier;
            this.challenge = challenge;
            this.challengeMethod = challengeMethod;
        }
    }
}
