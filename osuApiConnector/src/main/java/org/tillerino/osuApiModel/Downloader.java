package org.tillerino.osuApiModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.annotation.CheckForNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BeatmapSetId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.UserId;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class Downloader {
	public static final String API_BASE_URL = "https://osu.ppy.sh/api/";

	public static final String GET_BEATMAPS = "get_beatmaps";
	
	public static final String GET_USER_BEST = "get_user_best";
	
	public static final String GET_SCORES = "get_scores";
	
	public static final String GET_USER = "get_user";
	
	public static final String GET_USER_RECENT = "get_user_recent";

	static final ObjectMapper JACKSON = new ObjectMapper()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	private final String baseUrl;

	private final String key;

	public static final String INVALID_API_KEY = "Please provide a valid API key.";

	private final static Pattern keyPattern = Pattern.compile("[0-9a-f]{40}");

	public Downloader(URL baseUrl, String key) {
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
	 * @param key
	 *            valid api key
	 */
	public Downloader(String key) {
		this(null, key);
	}

	/**
	 * Constructs a Downloader which will look for an api key. It will first
	 * look for the system property "osuapikey" and then for a resource named
	 * "osuapikey", either of which should only contain a valid api key.
	 */
	public Downloader() {
		String systemKey = System.getProperty("osuapikey");
		if (systemKey == null) {
			systemKey = System.getenv("OSUAPIKEY");
		}

		if(systemKey != null) {
			if(!keyPattern.matcher(systemKey).matches()) {
				throw new RuntimeException("system property osuapikey found, but looks invalid");
			}
			this.key = systemKey;
		} else {
			InputStream is = Downloader.class.getClassLoader().getResourceAsStream("osuapikey");

			if(is == null) {
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
			if(!keyPattern.matcher(resourceKey).matches()) {
				throw new RuntimeException("resource osuapikey found, but looks invalid: " + resourceKey);
			}

			this.key = resourceKey;
		}

		this.baseUrl = API_BASE_URL;
	}

	@CheckForNull
	public <T extends OsuApiBeatmap> T getBeatmap(@BeatmapId int beatmapId, Class<T> cls) throws IOException {
		JsonNode array = get(GET_BEATMAPS, "b", String.valueOf(beatmapId));
		if(array.size() == 0) {
			return null;
		}
		return JACKSON.treeToValue(array.get(0), cls);
	}

	@CheckForNull
	public <T extends OsuApiBeatmap> T getBeatmap(@BeatmapId int beatmapId, long mods, Class<T> cls) throws IOException {
		JsonNode array = get(GET_BEATMAPS, "b", String.valueOf(beatmapId), "mods", String.valueOf(mods));
		if(array.size() == 0) {
			return null;
		}
		return JACKSON.treeToValue(array.get(0), cls);
	}

	/**
	 * 
	 * @param beatmapsetId
	 * @param cls the class of the desired results.
	 * @return the beatmap set. no particular order. null if nothing was returned.
	 * @throws IOException
	 */
	@CheckForNull
	public <T extends OsuApiBeatmap> List<T> getBeatmapSet(@BeatmapSetId int beatmapsetId, Class<T> cls) throws IOException {
		JsonNode array = get(GET_BEATMAPS, "s", String.valueOf(beatmapsetId));
		if(array.size() == 0) {
			return null;
		}
		return JACKSON.treeToValue(array, TypeFactory.defaultInstance().constructCollectionType(List.class, cls));
	}

	public JsonNode get(String command, String... parameters)
			throws IOException {
		URL url = formURL(true, command, parameters);
		String content;
		try {
			content = downloadDirect(url);
		} catch (SocketTimeoutException e) {
			throw e;
		} catch (IOException e1) {
			throw new IOException(e1.getMessage() + " for " + formURL(false, command, parameters), e1);
		}
		if(content.equals(INVALID_API_KEY))
			throw new RuntimeException(INVALID_API_KEY);
		try {
			return JACKSON.readTree(content);
		} catch (JsonProcessingException e) {
			String shortContent = content.substring(0, Math.min(content.length(), 100)) + (content.length() < 100 ? "..." : "");
			throw new RuntimeException("Unable to parse response: " + shortContent, e);
		}
	}

	public URL formURL(boolean addKey, String command, String... parameters)
			throws IOException {
		if (parameters.length % 2 != 0) {
			throw new IllegalArgumentException("must provide key value pairs!");
		}

		StringBuilder builder = new StringBuilder(baseUrl);
		builder.append(command);
		if(addKey) {
			builder.append("?k=");
			builder.append(key);
		}
		for (int i = 0; i < parameters.length; i += 2) {
			builder.append("&");
			builder.append(parameters[i]);
			builder.append("=");
			builder.append(URLEncoder.encode(parameters[i + 1], "UTF-8"));
		}

		URL url = new URL(builder.toString());
		return url;
	}

	public static String downloadDirect(URL url) throws IOException {
		return downloadDirect(url, 5000);
	}

	public static String downloadDirect(URL url, int timeout) throws IOException {
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
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
	
				if (httpCon.getContentType() == null
						|| !httpCon.getContentType().contains("application/json;")
						|| !httpCon.getContentType().toLowerCase().contains("charset=utf-8")) {
					throw new IOException("unexpected content-type: "
							+ httpCon.getContentType());
				}
	
				byte[] buf = new byte[1024];
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				for(int len; (len = inputStream.read(buf)) > 0;) {
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
	 * @param userId user's integer id
	 * @param mode game mode (see {@link GameModes})
	 * @param limit number of entries to retreive, 1-50
	 * @param cls desired object class
	 * @return
	 * @throws IOException
	 */
	public <T extends OsuApiScore> List<T> getUserTop(@UserId int userId, @GameMode int mode, int limit, Class<T> cls) throws IOException {
		JsonNode jsonArray = get(GET_USER_BEST, "u", String.valueOf(userId), "m", String.valueOf(mode), "limit", String.valueOf(limit), "type", "id");

		return OsuApiScore.fromJsonArray(toArray(jsonArray), cls, mode);
	}
	
	/**
	 * gets a the top scores for a beatmap
	 * @param beatmapId beatmap id
	 * @param mode game mode (see {@link GameModes})
	 * @param cls desired object class
	 * @return
	 * @throws IOException
	 */
	public <T extends OsuApiScore> List<T> getBeatmapTop(@BeatmapId int beatmapId, @GameMode int mode, Class<T> cls) throws IOException {
		ArrayNode jsonArray = toArray(get(GET_SCORES, "b", String.valueOf(beatmapId), "m", String.valueOf(mode)));

		for (JsonNode node : jsonArray) {
			toObject(node).put("beatmap_id", beatmapId);
		}

		return OsuApiScore.fromJsonArray(jsonArray, cls, mode);
	}

	/**
	 * gets a the top scores for a beatmap
	 * @param beatmapId beatmap id
	 * @param mode game mode (see {@link GameModes})
	 * @param cls desired object class
	 * @param mods mods
	 * @return
	 * @throws IOException
	 */
	public <T extends OsuApiScore> List<T> getBeatmapTop(@BeatmapId int beatmapId, @GameMode int mode, Class<T> cls, @BitwiseMods long mods) throws IOException {
		ArrayNode jsonArray = toArray(get(GET_SCORES, "b", String.valueOf(beatmapId), "m", String.valueOf(mode), "mods", String.valueOf(mods)));

		for (JsonNode node : jsonArray) {
			toObject(node).put("beatmap_id", beatmapId);
		}

		return OsuApiScore.fromJsonArray(jsonArray, cls, mode);
	}

	/**
	 * @deprecated because the API might return multiple scores
	 */
	@Deprecated
	@CheckForNull
	public <T extends OsuApiScore> T getScore(@UserId int userId, @BeatmapId int beatmapId, @GameMode int mode, Class<T> cls) throws IOException {
		JsonNode jsonElement = get(GET_SCORES, "b", String.valueOf(beatmapId), "u", String.valueOf(userId), "m", String.valueOf(mode));

		if(jsonElement.isNull())
			return null;

		ArrayNode jsonArray = toArray(jsonElement);

		if(jsonArray.isEmpty()) {
			return null;
		}

		// there might be more than one score. We get the
		ObjectNode jsonObject = toObject(jsonArray.get(0));

		jsonObject.put("beatmap_id", beatmapId);

		return OsuApiScore.fromJsonObject(jsonObject, cls, mode);
	}
	
	@CheckForNull
	public <T extends OsuApiUser> T getUser(@UserId int userId, @GameMode int mode, Class<T> cls) throws IOException {
		ArrayNode jsonArray = toArray(get(GET_USER, "u", String.valueOf(userId), "m", String.valueOf(mode), "type", "id"));

		if(jsonArray.size() == 0) {
			return null;
		}
		T user = OsuApiUser.fromJsonObject(jsonArray.get(0), cls, mode);
		user.setMode(mode);
		return user;
	}

	@CheckForNull
	public <T extends OsuApiUser> T getUser(String username, @GameMode int mode, Class<T> cls) throws IOException {
		ArrayNode jsonArray = toArray(get(GET_USER, "u", username, "m", String.valueOf(mode), "type", "string"));

		if(jsonArray.size() == 0) {
			return null;
		}
		T user = OsuApiUser.fromJsonObject(jsonArray.get(0), cls, mode);
		user.setMode(mode);
		return user;
	}
	
	public <T extends OsuApiScore> List<T> getUserRecent(@UserId int userid, @GameMode int mode, Class<T> cls) throws IOException {
		JsonNode jsonElement = get(GET_USER_RECENT, "u", String.valueOf(userid), "m", String.valueOf(mode), "type", "id");
		if (jsonElement instanceof NullNode) {
			return Collections.emptyList();
		}

		return OsuApiScore.fromJsonArray(toArray(jsonElement), cls, mode);
	}

	private static ArrayNode toArray(JsonNode n) {
		if (!n.isArray()) {
			throw new RuntimeException("Expected array");
		}
		return (ArrayNode) n;
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
	 * @return an implementation of {@link Downloader} which will load API objects
	 *         from the class path. It will load from /osuv1api/|path||?query| where
	 *         path is the path of the HTTP request and query is the query string
	 *         excluding the API key.
	 */
	public static Downloader createTestDownloader(Class<?> cls) {
		class FakeDownloader extends Downloader {
			public FakeDownloader() {
				super("0123456789012345678901234567890123456789");
			}

			@SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "false positive")
			@Override
			public JsonNode get(String command, String... parameters) throws IOException {
				URL url = formURL(false, command, parameters);
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
