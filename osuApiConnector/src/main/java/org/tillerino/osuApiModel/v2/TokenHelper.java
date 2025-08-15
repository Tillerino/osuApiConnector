package org.tillerino.osuApiModel.v2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.Setter;

public class TokenHelper {
    @Setter
    private static String testTokenOverride = null;

    private static final String CACHE_FILE = "token_cache.json";

    private static class CachedToken {
        String accessToken;
        long expiresAt;

        boolean isExpired() {
            return System.currentTimeMillis() >= expiresAt;
        }
    }

    public String getToken(String key) throws IOException {
        if (testTokenOverride != null) {
            return testTokenOverride;
        }

        CachedToken cachedToken = readCache();
        if (cachedToken != null && !cachedToken.isExpired()) {
            return cachedToken.accessToken;
        }

        TokenResponse response = requestNewToken(key);
        writeCache(response.accessToken(), System.currentTimeMillis() + response.expiresIn() * 1000L - 10_000);
        return response.accessToken();
    }

    private static void writeCache(String accessToken, long expiresAt) throws IOException {
        try (OutputStreamWriter writer =
                new OutputStreamWriter(Files.newOutputStream(Paths.get(CACHE_FILE)), StandardCharsets.UTF_8)) {
            writer.write("{\n");
            writer.write("  \"access_token\": \"" + accessToken + "\",\n");
            writer.write("  \"expires_at\": " + expiresAt + "\n");
            writer.write("}\n");
        }
    }

    private static CachedToken readCache() {
        File file = new File(CACHE_FILE);
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            String line;
            String accessToken = null;
            long expiresAt = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("\"access_token\"")) {
                    accessToken = line.split(":")[1].trim().replace("\"", "").replace(",", "");
                } else if (line.startsWith("\"expires_at\"")) {
                    expiresAt = Long.parseLong(line.split(":")[1].trim());
                }
            }

            if (accessToken != null && expiresAt > 0) {
                CachedToken token = new CachedToken();
                token.accessToken = accessToken;
                token.expiresAt = expiresAt;
                return token;
            }

        } catch (IOException | NumberFormatException e) {
            // Corrupt cache, treat as missing
        }

        return null;
    }

    // Reference: https://osu.ppy.sh/docs/index.html#client-credentials-grant
    private static TokenResponse requestNewToken(String key) throws IOException {
        String clientId = System.getenv("CLIENT_ID");

        if (clientId == null || key == null) {
            throw new IllegalStateException("Environment variables CLIENT_ID or OSUAPIKEY are not set.");
        }

        String urlParams = "client_id=" + URLEncoder.encode(clientId, "UTF-8") + "&client_secret="
                + URLEncoder.encode(key, "UTF-8") + "&grant_type=client_credentials"
                + "&scope=public";

        URL url = new URL("https://osu.ppy.sh/oauth/token");
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

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        String tokenJson = response.toString();
        String token = tokenJson.replaceAll(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        String expiresStr = tokenJson.replaceAll(".*\"expires_in\"\\s*:\\s*(\\d+).*", "$1");

        return new TokenResponse(token, Integer.parseInt(expiresStr));
    }

    public static record TokenResponse(String accessToken, int expiresIn) {}
}
