package org.tillerino.osuApiModel.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenHelper {
    private static final ObjectMapper JACKSON =
            new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    // Reference: https://osu.ppy.sh/docs/index.html#client-credentials-grant
    private static TokenResponse requestNewToken(URI base, String clientId, String clientSecret) throws IOException {
        log.info("Requesting new osu! API token for client ID {}", clientId);

        String urlParams = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) + "&client_secret="
                + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) + "&grant_type=client_credentials"
                + "&scope=public";

        URL url = base.resolve("/oauth/token").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(urlParams.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to get token: HTTP " + conn.getResponseCode());
        }

        return JACKSON.readValue(conn.getInputStream(), TokenResponse.class);
    }

    public abstract static class TokenCache {
        private final URI base;
        private final Credentials credentials;

        protected TokenCache(URI base, Credentials credentials) {
            this.base = base;
            this.credentials = credentials;
        }

        public String getToken() throws IOException {
            CachedToken cached = getCachedToken();
            OffsetDateTime now = OffsetDateTime.now();
            if (cached == null || cached.expiresAt().minusSeconds(10).isBefore(now)) {
                TokenResponse newToken = requestNewToken(base, credentials.clientId, credentials.clientSecret);
                cached = new CachedToken(newToken.accessToken, now.plusSeconds(newToken.expiresIn));
                cacheToken(cached);
            }
            return cached.token();
        }

        protected abstract CachedToken getCachedToken();

        protected abstract void cacheToken(CachedToken cached);

        public static TokenCache inFile(URI base, Credentials credentials, File file) {
            return new TokenCache(base, credentials) {
                @CheckForNull
                CachedToken cache = null;

                @Override
                protected CachedToken getCachedToken() {
                    if (cache == null && file.exists()) {
                        try {
                            cache = JACKSON.readValue(file, CachedToken.class);
                        } catch (IOException e) {
                            throw new IllegalStateException("Cache is not readable", e);
                        }
                    }
                    return cache;
                }

                @Override
                protected void cacheToken(CachedToken cached) {
                    cache = cached;
                    try {
                        JACKSON.writeValue(file, cached);
                    } catch (IOException e) {
                        throw new IllegalStateException("Cache is not writable", e);
                    }
                }
            };
        }

        public static TokenCache inMemory(URI base, Credentials credentials) {
            return new TokenCache(base, credentials) {
                CachedToken cache = null;

                @Override
                protected CachedToken getCachedToken() {
                    return cache;
                }

                @Override
                protected void cacheToken(CachedToken cached) {
                    cache = cached;
                }
            };
        }

        public static TokenCache constant(String token) {
            return new TokenCache(null, new Credentials("12345", "0123456789012345678901234567890123456789")) {
                @Override
                protected CachedToken getCachedToken() {
                    return new CachedToken(token, OffsetDateTime.now().plusYears(10));
                }

                @Override
                protected void cacheToken(CachedToken cached) {}
            };
        }

        protected record CachedToken(String token, OffsetDateTime expiresAt) {}
    }

    public record Credentials(String clientId, String clientSecret) {
        private static final Pattern idPattern = Pattern.compile("\\d+");
        private static final Pattern secretPattern = Pattern.compile("[A-Za-z0-9]{40}");

        public Credentials {
            if (!idPattern.matcher(clientId).matches()) {
                throw new IllegalArgumentException("Invalid osu! API client ID");
            }
            if (!secretPattern.matcher(clientSecret).matches()) {
                throw new IllegalArgumentException("Invalid osu! API client secret");
            }
        }

        public static Credentials fromEnvOrProps() {
            String osuApiClientId = Optional.ofNullable(System.getenv("OSU_API_CLIENT_ID"))
                    .or(() -> Optional.ofNullable(System.getProperty("osu.api.client.id")))
                    .orElseThrow(
                            () -> new NoSuchElementException(
                                    "No osu! API client ID found. Set the OSU_API_CLIENT_ID environment variable or the osu.api.client.id system property."));
            String osuApiClientSecret = Optional.ofNullable(System.getenv("OSU_API_CLIENT_SECRET"))
                    .or(() -> Optional.ofNullable(System.getProperty("osu.api.client.secret")))
                    .orElseThrow(
                            () -> new NoSuchElementException(
                                    "No osu! API client ID found. Set the OSU_API_CLIENT_SECRET environment variable or the osu.api.client.secret system property."));
            return new Credentials(osuApiClientId, osuApiClientSecret);
        }
    }

    record TokenResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") int expiresIn) {}
}
