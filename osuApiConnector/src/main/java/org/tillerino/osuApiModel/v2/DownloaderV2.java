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
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
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

public class DownloaderV2 {
    public static final String API_BASE_URL = "https://osu.ppy.sh/api/v2/";

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

    private final String key;

    public static final String INVALID_API_KEY = "Please provide a valid API key.";

    private static final Pattern keyPattern = Pattern.compile("[A-Za-z0-9]{40}");

    public DownloaderV2(URL baseUrl, String key) {
        if (baseUrl != null) {
            this.baseUrl = baseUrl.toString();
        } else {
            this.baseUrl = API_BASE_URL;
        }
        this.key = key;
    }

    /**
     * Constructs a Downloader with the given api key.
     *
     * @param key valid api key
     */
    public DownloaderV2(String key) {
        this(null, key);
    }

    /**
     * Constructs a Downloader which will look for an api key. It will first
     * look for the system property "osuapikey" and then for a resource named
     * "osuapikey", either of which should only contain a valid api key.
     */
    public DownloaderV2() {
        String systemKey = System.getProperty("osuapikey");
        if (systemKey == null) {
            systemKey = System.getenv("OSUAPIKEY");
        }

        if (systemKey != null) {
            if (!keyPattern.matcher(systemKey).matches()) {
                throw new RuntimeException("system property osuapikey found, but looks invalid");
            }
            this.key = systemKey;
        } else {
            InputStream is = DownloaderV2.class.getClassLoader().getResourceAsStream("osuapikey");

            if (is == null) {
                throw new RuntimeException("No api key found.");
            }

            byte[] buf = new byte[40];
            int len;
            try {
                len = is.read(buf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String resourceKey = new String(buf, 0, len, StandardCharsets.UTF_8);
            if (!keyPattern.matcher(resourceKey).matches()) {
                throw new RuntimeException("resource osuapikey found, but looks invalid: " + resourceKey);
            }

            this.key = resourceKey;
        }

        this.baseUrl = API_BASE_URL;
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
        URL url = formURL(command, parameters);
        TokenHelper tokenHelper = new TokenHelper();
        String token = tokenHelper.getToken(key);
        String content;

        try {
            content = downloadDirect(url, token, method);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e1) {
            throw new IOException(e1.getMessage() + " for " + formURL(command, parameters), e1);
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

    public URL formURL(String command, String... parameters) throws IOException {
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
                builder.append(URLEncoder.encode(parameters[i + 1], "UTF-8"));
            }
        }

        return new URL(builder.toString());
    }

    public static String downloadDirect(URL url, String key, String method) throws IOException {
        return downloadDirect(url, 5000, key, method);
    }

    public static String downloadDirect(URL url, int timeout, String key, String method) throws IOException {
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

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
     * gets a the top scores for a beatmap
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
                super("0123456789012345678901234567890123456789");
            }

            @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "false positive")
            @Override
            public JsonNode get(String command, String method, String... parameters) throws IOException {
                URL url = formURL(command, parameters);
                if (url.toString().contains("&") && !url.toString().contains("?")) {
                    url = new URL(url.toString().replaceFirst("&", "?"));
                }
                String relPath = "/osuv1api" + url.getPath() + URLEncoder.encode("?" + url.getQuery(), "UTF-8");
                try (InputStream in = cls.getResourceAsStream(relPath)) {
                    if (in == null) {
                        throw new RuntimeException("Resource not found: " + relPath + " " + url);
                    }
                    return JACKSON.readTree(in);
                }
            }
        }
        return new FakeDownloader();
    }
}
