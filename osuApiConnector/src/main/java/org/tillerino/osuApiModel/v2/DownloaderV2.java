package org.tillerino.osuApiModel.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import javax.annotation.CheckForNull;
import org.mapstruct.factory.Mappers;
import org.tillerino.osuApiModel.*;
import org.tillerino.osuApiModel.types.*;
import org.tillerino.osuApiModel.v2.TokenHelper.TokenCache;

public class DownloaderV2 implements OsuApiClient {
    public static final URI PROD_API_BASE = URI.create("https://osu.ppy.sh/api/v2/");

    static final V2Mapper MAPPER = Mappers.getMapper(V2Mapper.class);

    static final ObjectMapper JACKSON = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final String baseUrl;

    private final TokenCache tokenCache;

    public static final String INVALID_API_KEY = "Please provide a valid API key.";

    public DownloaderV2(URI baseUrl, TokenCache tokenCache) {
        this.baseUrl = baseUrl.toString();
        this.tokenCache = tokenCache;
    }

    /**
     * Constructs a Downloader with the given api key.
     *
     * @param tokenCache the token cache to use
     */
    public DownloaderV2(TokenCache tokenCache) {
        this(PROD_API_BASE, tokenCache);
    }

    /**
     * Constructs a Downloader which will look for an api key. It will first
     * look for the system property "osuapikey" and then for a resource named
     * "osuapikey", either of which should only contain a valid api key.
     */
    public DownloaderV2() {
        this(TokenCache.inMemory(TokenHelper.Credentials.fromEnvOrProps()));
    }

    @CheckForNull
    public <T extends OsuApiBeatmap> T getBeatmap(@BeatmapId int beatmapId, @BitwiseMods long mods, Class<T> cls)
            throws IOException {
        record BeatmapAttributesRequestBody(@BitwiseMods long mods) {}

        // Required to retrieve the same information that was retrieved on the old get_beatmaps endpoint
        JsonNode beatmapInfoNode = fetch("beatmaps/{beatmap}", "GET", null, "{beatmap}", beatmapId);
        JsonNode beatmapAttrNode = fetch("beatmaps/{beatmap}/attributes", "POST", new BeatmapAttributesRequestBody(mods), "{beatmap}", beatmapId);

        JsonNode beatmapData = ((ObjectNode) beatmapInfoNode).setAll((ObjectNode) beatmapAttrNode);
        if (beatmapData.isEmpty()) {
            return null;
        }

        OsuApiBeatmapV2 beatmapNode = JACKSON.treeToValue(beatmapData, OsuApiBeatmapV2.class);
        return MAPPER.mapBeatmapToV1(beatmapNode, cls);
    }

    /**
     * @param beatmapsetId
     * @param cls
     * @return the beatmap set. no particular order. null if nothing was returned.
     * @throws IOException
     */
    @CheckForNull
    public <T extends OsuApiBeatmap> List<T> getBeatmapSet(@BeatmapSetId int beatmapsetId, Class<T> cls)
            throws IOException {
        JsonNode array = fetch("beatmapsets/{beatmapset}", "GET", null, "{beatmapset}", beatmapsetId);
        if (array.isEmpty()) {
            return null;
        }

        return JACKSON.treeToValue(array, TypeFactory.defaultInstance().constructCollectionType(List.class, cls));
    }

    public JsonNode fetch(String command, String method, @CheckForNull Object requestBody, Object... parameters)
            throws IOException {
        URI uri = formURI(command, parameters);
        String token = tokenCache.getToken();
        String content;

        try {
            content = downloadDirect(uri, token, method, requestBody);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e1) {
            throw new IOException(e1.getMessage() + " for " + uri, e1);
        }
        if (content.equals(INVALID_API_KEY)) throw new RuntimeException(INVALID_API_KEY);
        try {
            return JACKSON.readTree(content);
        } catch (JsonProcessingException e) {
            String shortContent =
                    content.substring(0, Math.min(content.length(), 100)) + (content.length() < 100 ? "..." : "");
            throw new RuntimeException("Unable to parse response: " + shortContent, e);
        }
    }

    public URI formURI(String command, Object... parameters) {
        if (parameters.length % 2 != 0) {
            throw new IllegalArgumentException("must provide key value pairs!");
        }

        for (int i = 0; i < parameters.length; i += 2) {
            if (!(parameters[i] instanceof String s)) {
                throw new IllegalArgumentException("Not a String: " + parameters[i]);
            }
            if (!command.contains(s)) {
                throw new IllegalArgumentException("command must contain parameter " + s + "!");
            }
            command = command.replace(
                    s,
                    URLEncoder.encode(String.valueOf(parameters[i + 1]), StandardCharsets.UTF_8));
        }

        return URI.create(baseUrl + command);
    }

    public static String downloadDirect(URI uri, String key, String method, Object requestBody) throws IOException {
        return downloadDirect(uri, 5000, key, method, requestBody);
    }

    public static String downloadDirect(URI uri, int timeout, String key, String method, Object requestBody)
            throws IOException {
        HttpURLConnection httpCon = (HttpURLConnection) uri.toURL().openConnection();

        httpCon.setRequestMethod(method);
        httpCon.setRequestProperty("Authorization", "Bearer " + key);
        httpCon.setRequestProperty("Accept-Encoding", "gzip");
        httpCon.setConnectTimeout(timeout);
        httpCon.setReadTimeout(timeout);

        try {
            if (requestBody != null) {
                httpCon.setDoOutput(true);
                httpCon.setRequestProperty("Content-Type", "application/json");
                JACKSON.writeValue(httpCon.getOutputStream(), requestBody);
            }

            if (httpCon.getResponseCode() != 200) {
                throw new IOException("response code " + httpCon.getResponseCode());
            }

            String contentEncoding = httpCon.getContentEncoding();
            InputStream inputStream = httpCon.getInputStream();
            try {
                if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
                    inputStream = new GZIPInputStream(inputStream);
                }

                String contentType = httpCon.getContentType();
                if (contentType == null || !contentType.contains("application/json")) {
                    throw new IOException("unexpected content-type: " + contentType);
                }

                if (!contentType.toLowerCase().contains("charset=utf-8")) {
                    System.err.println("Warning: unexpected charset in content-type: " + contentType);
                }

                byte[] buf = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (int len; (len = inputStream.read(buf)) > 0; ) {
                    baos.write(buf, 0, len);
                }

                return baos.toString(StandardCharsets.UTF_8);
            } finally {
                inputStream.close();
            }
        } finally {
            httpCon.disconnect();
        }
    }

    /**
     * gets a user's top scores
     *
     * @param userId user's integer id
     * @param mode   game mode (see {@link GameModes})
     * @param limit  number of entries to retreive, 1-50
     * @return
     * @throws IOException
     */
    public <T extends OsuApiScore> List<T> getUserTop(@UserId int userId, @GameMode int mode, int limit, Class<T> cls)
            throws IOException {
        JsonNode jsonArray = fetch(
                "users/{user}/scores/best?mode={mode}&limit={limit}",
                "GET",
                null,
                "{user}",
                userId,
                "{mode}",
                GameModes.getRulesetName(mode),
                "{limit}",
                limit);

        List<OsuApiScoreV2> scores = new ArrayList<>();
        for (JsonNode elem : jsonArray) {
            OsuApiScoreV2 scoreV2 = JACKSON.treeToValue(elem, OsuApiScoreV2.class);
            scores.add(scoreV2);
        }

        return scores.stream().map(scoreV2 -> MAPPER.mapScoreToV1(scoreV2, cls)).collect(Collectors.toList());
    }

    /**
     * gets a the top scores for a beatmap
     *
     * @param beatmapId beatmap id
     * @param mode      game mode (see {@link GameModes})
     * @return
     * @throws IOException
     */
    public <T extends OsuApiScore> List<T> getBeatmapTop(@BeatmapId int beatmapId, @GameMode int mode, Class<T> cls)
            throws IOException {
        ArrayNode jsonArray = toArray(fetch(
                "beatmaps/{beatmap}/scores?mode={mode}",
                "GET",
                null,
                "{beatmap}",
                beatmapId,
                "{mode}",
                GameModes.getRulesetName(mode)));

        List<OsuApiScoreBeatmapV2> scores = new ArrayList<>();
        for (JsonNode elem : jsonArray) {
            OsuApiScoreBeatmapV2 scoreV2 = JACKSON.treeToValue(elem, OsuApiScoreBeatmapV2.class);
            scores.add(scoreV2);
        }

        return scores.stream()
                .map(scoreV2 -> MAPPER.mapBeatmapScoreToV1(scoreV2, cls))
                .collect(Collectors.toList());
    }

    /**
     * gets the top scores for a beatmap
     *
     * @param beatmapId beatmap id
     * @param mode      game mode (see {@link GameModes})
     * @param mods      mods
     * @return
     * @throws IOException
     */
    public <T extends OsuApiScore> List<T> getBeatmapTop(
            @BeatmapId int beatmapId, @GameMode int mode, String[] mods, Class<T> cls) throws IOException {
        String modeRuleset = GameModes.getRulesetName(mode);

        StringBuilder modsQuery = new StringBuilder();
        for (String mod : mods) {
            modsQuery.append("&mods[]=").append(URLEncoder.encode(mod, StandardCharsets.UTF_8));
        }

        ArrayNode jsonArray = toArray(fetch(
                "beatmaps/{beatmap}/scores?mode={mode}" + modsQuery,
                "GET",
                null,
                "{beatmap}",
                beatmapId,
                "{mode}",
                modeRuleset));
        ArrayNode scoresArray = (ArrayNode) jsonArray.get(0).get("scores");

        List<OsuApiScoreBeatmapV2> scores = new ArrayList<>();
        for (JsonNode elem : scoresArray) {
            OsuApiScoreBeatmapV2 scoreV2 = JACKSON.treeToValue(elem, OsuApiScoreBeatmapV2.class);
            scores.add(scoreV2);
        }

        return scores.stream()
                .map(scoreV2 -> MAPPER.mapBeatmapScoreToV1(scoreV2, cls))
                .collect(Collectors.toList());
    }

    /**
     * @deprecated because the API might return multiple scores
     */
    @Deprecated
    @CheckForNull
    public <T extends OsuApiScore> T getScore(
            @UserId int userId, @BeatmapId int beatmapId, @GameMode int mode, Class<T> cls) throws IOException {
        JsonNode jsonElement = fetch(
                "beatmaps/{beatmap}/scores/users/{user}?mode={mode}",
                "GET",
                null,
                "{beatmap}",
                beatmapId,
                "{user}",
                userId,
                "{mode}",
                GameModes.getRulesetName(mode));

        if (jsonElement.isNull()) return null;

        ArrayNode jsonArray = toArray(jsonElement);

        if (jsonArray.isEmpty()) {
            return null;
        }

        // there might be more than one score. We get the first one
        ObjectNode jsonObject = toObject(jsonArray.get(0).path("score"));

        OsuApiScoreV2 scoreV2 = JACKSON.treeToValue(jsonObject, OsuApiScoreV2.class);
        return MAPPER.mapScoreToV1(scoreV2, cls);
    }

    @CheckForNull
    public <T extends OsuApiUser> T getUser(@UserId int userId, @GameMode int mode, Class<T> cls) throws IOException {
        ArrayNode jsonArray = toArray(fetch(
                "users/{user}?mode={mode}&key=id".replace("{user}", String.valueOf(userId)),
                "GET",
                null,
                "{mode}",
                GameModes.getRulesetName(mode)));

        if (jsonArray.isEmpty()) {
            return null;
        }

        OsuApiUserV2 userV2 = JACKSON.treeToValue(jsonArray.get(0), OsuApiUserV2.class);
        T user = MAPPER.mapUserToV1(userV2, cls);
        user.setMode(mode);

        return user;
    }

    @CheckForNull
    public <T extends OsuApiUser> T getUser(String username, @GameMode int mode, Class<T> cls) throws IOException {
        ArrayNode jsonArray = toArray(fetch(
                "users/{user}?mode={mode}&key=username".replace("{user}", username),
                "GET",
                null,
                "{mode}",
                GameModes.getRulesetName(mode)));

        if (jsonArray.isEmpty()) {
            return null;
        }

        OsuApiUserV2 userV2 = JACKSON.treeToValue(jsonArray.get(0), OsuApiUserV2.class);
        T user = MAPPER.mapUserToV1(userV2, cls);
        user.setMode(mode);

        return user;
    }

    public <T extends OsuApiScore> List<T> getUserRecent(@UserId int userid, @GameMode int mode, Class<T> cls)
            throws IOException {
        JsonNode jsonElement = fetch(
                "users/{user}/scores/recent?mode={mode}&limit={limit}",
                "GET",
                null,
                "{user}",
                userid,
                "{mode}",
                GameModes.getRulesetName(mode),
                "{limit}",
                "10");
        if (jsonElement instanceof NullNode) {
            return Collections.emptyList();
        }

        List<OsuApiScoreV2> scores = new ArrayList<>();
        for (JsonNode elem : jsonElement) {
            OsuApiScoreV2 scoreV2 = JACKSON.treeToValue(elem, OsuApiScoreV2.class);
            scores.add(scoreV2);
        }

        return scores.stream().map(scoreV2 -> MAPPER.mapScoreToV1(scoreV2, cls)).collect(Collectors.toList());
    }

    private static ArrayNode toArray(JsonNode n) {
        if (n.isArray()) {
            return (ArrayNode) n;
        } else if (n.isObject()) {
            ArrayNode arr = JACKSON.createArrayNode();
            arr.add(n);
            return arr;
        }

        throw new RuntimeException("Expected array");
    }

    private static ObjectNode toObject(JsonNode n) {
        if (!n.isObject()) {
            throw new RuntimeException("Expected array");
        }
        return (ObjectNode) n;
    }

    /**
     * Creates a test implementation that loads API objects from the classpath.
     *
     * @param cls A class from the class loader that can load the resources. If you
     *            are creating this in a unit test where the resources are in the
     *            same module, getClass() will probably work just fine.
     * @return an implementation of {@link DownloaderV2} which will load API objects
     * from the class path. It will load from /osuv1api/|path||?query| where
     * path is the path of the HTTP request and query is the query string
     * excluding the API key.
     */
    public static DownloaderV2 createTestDownloader(Class<?> cls) {
        class FakeDownloader extends DownloaderV2 {
            public FakeDownloader() {
                super(TokenHelper.TokenCache.constant("fake"));
            }

            @Override
            public JsonNode fetch(String command, String method, Object requestBody, Object... parameters)
                    throws IOException {
                URI uri = formURI(command, parameters);
                if (uri.toString().contains("&") && !uri.toString().contains("?")) {
                    uri = URI.create(uri.toString().replaceFirst("&", "?"));
                }
                String relPath = "/osuv1api" + uri.getPath() + URLEncoder.encode("?" + uri.getQuery(), "UTF-8");
                try (InputStream in = cls.getResourceAsStream(relPath)) {
                    if (in == null) {
                        throw new RuntimeException("Resource not found: " + relPath + " " + uri);
                    }
                    return JACKSON.readTree(in);
                }
            }
        }
        return new FakeDownloader();
    }
}
