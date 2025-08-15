package org.tillerino.osuApiModel.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BeatmapSetId;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.osuApiModel.v2.TokenHelper.TokenCache;

public class DownloaderV2 {
    public static final URI PROD_API_BASE = URI.create("https://osu.ppy.sh/api/v2/");

    public static final String GET_BEATMAPS = "beatmaps/{beatmap}";

    public static final String GET_USER_BEST = "users/{user}/scores/best";

    public static final String GET_SCORES = "beatmaps/{beatmap}/scores";

    public static final String GET_USER = "users/{user}";

    public static final String GET_USER_RECENT = "users/{user}/scores/recent";

    public static final String GET_USER_SCORES_BY_BEATMAP = "beatmaps/{beatmap}/scores/users/{user}";

    public static final String GET_BEATMAP_ATTRIBUTES = "beatmaps/{beatmap}/attributes";

    public static final String GET_BEATMAPSETS = "beatmapsets/{beatmapset}";

    public static final V2Mapper MAPPER = Mappers.getMapper(V2Mapper.class);

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

    private JsonNode getBeatmapData(int beatmapId, String... parameters) throws IOException {
        // Required to retrieve the same information that was retrieved on the old get_beatmaps endpoint
        JsonNode beatmapInfoNode = get(GET_BEATMAPS.replace("{beatmap}", String.valueOf(beatmapId)), "GET");
        JsonNode beatmapAttrNode =
                get(GET_BEATMAP_ATTRIBUTES.replace("{beatmap}", String.valueOf(beatmapId)), "POST", parameters);

        return ((ObjectNode) beatmapInfoNode).setAll((ObjectNode) beatmapAttrNode);
    }

    @CheckForNull
    public OsuApiBeatmap getBeatmap(@BeatmapId int beatmapId) throws IOException {
        JsonNode beatmapData = getBeatmapData(beatmapId);
        if (beatmapData.isEmpty()) {
            return null;
        }

        OsuApiBeatmapV2 beatmapNode = JACKSON.treeToValue(beatmapData, OsuApiBeatmapV2.class);
        return MAPPER.mapBeatmapToV1(beatmapNode);
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
        JsonNode array = get(GET_BEATMAPSETS.replace("{beatmapset}", String.valueOf(beatmapsetId)), "GET");
        if (array.size() == 0) {
            return null;
        }

        return JACKSON.treeToValue(array, TypeFactory.defaultInstance().constructCollectionType(List.class, cls));
    }

    public JsonNode get(String command, String method, String... parameters) throws IOException {
        URI uri = formURI(command, parameters);
        String token = tokenCache.getToken();
        String content;

        try {
            content = downloadDirect(uri, token, method);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e1) {
            throw new IOException(e1.getMessage() + " for " + formURI(command, parameters), e1);
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

    public URI formURI(String command, String... parameters) throws IOException {
        if (parameters.length % 2 != 0) {
            throw new IllegalArgumentException("must provide key value pairs!");
        }

        StringBuilder builder = new StringBuilder(baseUrl);
        builder.append(command);

        if (parameters.length > 0) {
            builder.append("?");
            for (int i = 0; i < parameters.length; i += 2) {
                if (i > 0) {
                    builder.append("&");
                }
                builder.append(parameters[i]);
                builder.append("=");
                builder.append(URLEncoder.encode(parameters[i + 1], StandardCharsets.UTF_8));
            }
        }

        return URI.create(builder.toString());
    }

    public static String downloadDirect(URI uri, String key, String method) throws IOException {
        return downloadDirect(uri, 5000, key, method);
    }

    public static String downloadDirect(URI uri, int timeout, String key, String method) throws IOException {
        HttpURLConnection httpCon = (HttpURLConnection) uri.toURL().openConnection();

        httpCon.setRequestMethod(method);
        httpCon.setRequestProperty("Authorization", "Bearer " + key);
        httpCon.setRequestProperty("Accept-Encoding", "gzip");
        httpCon.setConnectTimeout(timeout);
        httpCon.setReadTimeout(timeout);

        try {
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

                return baos.toString("UTF-8");
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
    public List<OsuApiScore> getUserTop(@UserId int userId, @GameMode int mode, int limit) throws IOException {
        String modeRuleset = GameModes.getRulesetName(mode);
        JsonNode jsonArray = get(
                GET_USER_BEST.replace("{user}", String.valueOf(userId)),
                "GET",
                "mode",
                modeRuleset,
                "limit",
                String.valueOf(limit));

        List<OsuApiScoreV2> scores = new ArrayList<>();
        for (JsonNode elem : jsonArray) {
            OsuApiScoreV2 scoreV2 = JACKSON.treeToValue(elem, OsuApiScoreV2.class);
            scores.add(scoreV2);
        }

        return scores.stream().map(MAPPER::mapScoreToV1).collect(Collectors.toList());
    }

    /**
     * gets a the top scores for a beatmap
     *
     * @param beatmapId beatmap id
     * @param mode      game mode (see {@link GameModes})
     * @return
     * @throws IOException
     */
    public List<OsuApiScore> getBeatmapTop(@BeatmapId int beatmapId, @GameMode int mode) throws IOException {
        String modeRuleset = GameModes.getRulesetName(mode);
        ArrayNode jsonArray =
                toArray(get(GET_SCORES.replace("{beatmap}", String.valueOf(beatmapId)), "GET", "mode", modeRuleset));

        List<OsuApiScoreBeatmapV2> scores = new ArrayList<>();
        for (JsonNode elem : jsonArray) {
            OsuApiScoreBeatmapV2 scoreV2 = JACKSON.treeToValue(elem, OsuApiScoreBeatmapV2.class);
            scores.add(scoreV2);
        }

        return scores.stream().map(MAPPER::mapBeatmapScoreToV1).collect(Collectors.toList());
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
    public List<OsuApiScore> getBeatmapTop(@BeatmapId int beatmapId, @GameMode int mode, String[] mods)
            throws IOException {
        String modeRuleset = GameModes.getRulesetName(mode);

        List<String> params = new ArrayList<>();
        params.add("mode");
        params.add(modeRuleset);

        for (String mod : mods) {
            params.add("mods[]");
            params.add(mod);
        }

        ArrayNode jsonArray = toArray(
                get(GET_SCORES.replace("{beatmap}", String.valueOf(beatmapId)), "GET", params.toArray(new String[0])));
        ArrayNode scoresArray = (ArrayNode) jsonArray.get(0).get("scores");

        List<OsuApiScoreBeatmapV2> scores = new ArrayList<>();
        for (JsonNode elem : scoresArray) {
            OsuApiScoreBeatmapV2 scoreV2 = JACKSON.treeToValue(elem, OsuApiScoreBeatmapV2.class);
            scores.add(scoreV2);
        }

        return scores.stream().map(MAPPER::mapBeatmapScoreToV1).collect(Collectors.toList());
    }

    /**
     * @deprecated because the API might return multiple scores
     */
    @Deprecated
    @CheckForNull
    public OsuApiScore getScore(@UserId int userId, @BeatmapId int beatmapId, @GameMode int mode) throws IOException {
        String modeRuleset = GameModes.getRulesetName(mode);
        JsonNode jsonElement = get(
                GET_USER_SCORES_BY_BEATMAP
                        .replace("{beatmap}", String.valueOf(beatmapId))
                        .replace("{user}", String.valueOf(userId)),
                "GET",
                "mode",
                modeRuleset);

        if (jsonElement.isNull()) return null;

        ArrayNode jsonArray = toArray(jsonElement);

        if (jsonArray.isEmpty()) {
            return null;
        }

        // there might be more than one score. We get the first one
        ObjectNode jsonObject = toObject(jsonArray.get(0).path("score"));

        OsuApiScoreV2 scoreV2 = JACKSON.treeToValue(jsonObject, OsuApiScoreV2.class);
        return MAPPER.mapScoreToV1(scoreV2);
    }

    @CheckForNull
    public OsuApiUser getUser(@UserId int userId, @GameMode int mode) throws IOException {
        String modeRuleset = GameModes.getRulesetName(mode);
        ArrayNode jsonArray = toArray(
                get(GET_USER.replace("{user}", String.valueOf(userId)), "GET", "mode", modeRuleset, "key", "id"));

        if (jsonArray.size() == 0) {
            return null;
        }

        OsuApiUserV2 userV2 = JACKSON.treeToValue(jsonArray.get(0), OsuApiUserV2.class);
        OsuApiUser user = MAPPER.mapUserToV1(userV2);
        user.setMode(mode);

        return user;
    }

    @CheckForNull
    public OsuApiUser getUser(String username, @GameMode int mode) throws IOException {
        String modeRuleset = GameModes.getRulesetName(mode);
        ArrayNode jsonArray =
                toArray(get(GET_USER.replace("{user}", username), "GET", "mode", modeRuleset, "key", "username"));

        if (jsonArray.size() == 0) {
            return null;
        }

        OsuApiUserV2 userV2 = JACKSON.treeToValue(jsonArray.get(0), OsuApiUserV2.class);
        OsuApiUser user = MAPPER.mapUserToV1(userV2);
        user.setMode(mode);

        return user;
    }

    public List<OsuApiScore> getUserRecent(@UserId int userid, @GameMode int mode) throws IOException {
        String modeRuleset = GameModes.getRulesetName(mode);
        JsonNode jsonElement = get(
                GET_USER_RECENT.replace("{user}", String.valueOf(userid)), "GET", "mode", modeRuleset, "limit", "10");
        if (jsonElement instanceof NullNode) {
            return Collections.emptyList();
        }

        List<OsuApiScoreV2> scores = new ArrayList<>();
        for (JsonNode elem : jsonElement) {
            OsuApiScoreV2 scoreV2 = JACKSON.treeToValue(elem, OsuApiScoreV2.class);
            scores.add(scoreV2);
        }

        return scores.stream().map(MAPPER::mapScoreToV1).collect(Collectors.toList());
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

            @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "false positive")
            @Override
            public JsonNode get(String command, String method, String... parameters) throws IOException {
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
