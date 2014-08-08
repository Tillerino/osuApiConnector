package org.tillerino.osuApiModel;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class Downloader {
	public static final String API_BASE_URL = "http://osu.ppy.sh/api/";

	public static final String GET_BEATMAPS = "get_beatmaps";

	private final String key;

	public static final String INVALID_API_KEY = "Please provide a valid API key.";

	private final static Pattern keyPattern = Pattern.compile("[0-9a-f]{40}");

	/**
	 * Constructs a Downloader with the given api key.
	 * 
	 * @param key
	 *            valid api key
	 */
	public Downloader(String key) {
		super();
		this.key = key;
	}

	/**
	 * Constructs a Downloader which will look for an api key. It will first
	 * look for the system property "osuapikey" and then for a resource named
	 * "osuapikey", either of which should only contain a valid api key.
	 */
	public Downloader() {
		String systemKey = System.getProperty("osuapikey");

		if(systemKey != null) {
			if(!keyPattern.matcher(systemKey).matches()) {
				throw new RuntimeException("system property osuapikey found, but looks invalid: " + systemKey);
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

			String resourceKey = new String(buf, 0, len);
			if(!keyPattern.matcher(resourceKey).matches()) {
				throw new RuntimeException("resource osuapikey found, but looks invalid: " + resourceKey);
			}

			this.key = resourceKey;
		}
	}

	public <T extends OsuApiBeatmap> T getBeatmap(int beatmapId, Class<T> cls) throws IOException {
		JsonArray array = (JsonArray) get(GET_BEATMAPS, "b",
				String.valueOf(beatmapId));
		if(array.size() == 0) {
			return null;
		}
		return OsuApiBeatmap.fromJsonObject((JsonObject) array.get(0), cls);
	}

	/**
	 * 
	 * @param beatmapsetId
	 * @param cls the class of the desired results.
	 * @return the beatmap set. no particular order. null if nothing was returned.
	 * @throws IOException
	 */
	public <T extends OsuApiBeatmap> List<T> getBeatmapSet(int beatmapsetId, Class<T> cls) throws IOException {
		JsonArray jsonArray = (JsonArray) get(GET_BEATMAPS, "s",
				String.valueOf(beatmapsetId));
		if(jsonArray.size() == 0) {
			return null;
		}
		return OsuApiBeatmap.fromJsonArray(jsonArray, cls);
	}

	public JsonElement get(String command, String... parameters)
			throws IOException {
		URL url = formURL(command, parameters);
		String content = downloadDirect(url);
		if(content.equals(INVALID_API_KEY))
			throw new RuntimeException(INVALID_API_KEY);
		try {
			return new JsonParser().parse(content);
		} catch (JsonSyntaxException e) {
			String shortContent = content.substring(0, Math.min(content.length(), 100)) + (content.length() < 100 ? "..." : "");
			throw new RuntimeException("Unable to parse response: " + shortContent, e);
		}
	}

	public URL formURL(String command, String... parameters)
			throws MalformedURLException {
		if (parameters.length % 2 != 0) {
			throw new IllegalArgumentException("must provide key value pairs!");
		}

		StringBuilder builder = new StringBuilder(API_BASE_URL);
		builder.append(command);
		builder.append("?k=");
		builder.append(key);
		for (int i = 0; i < parameters.length; i += 2) {
			builder.append("&");
			builder.append(parameters[i]);
			builder.append("=");
			builder.append(parameters[i + 1]);
		}

		URL url = new URL(builder.toString());
		return url;
	}

	public static String downloadDirect(URL url) throws IOException {
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setRequestProperty("Accept-Encoding", "gzip");
		InputStream inputStream = httpCon.getInputStream();

		try {
			String contentEncoding = httpCon.getContentEncoding();
			if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
				inputStream = new GZIPInputStream(inputStream);
			}

			if (httpCon.getResponseCode() != 200) {
				throw new IOException("response code " + httpCon.getResponseCode());
			}

			if (!httpCon.getContentType().contains("application/json;") || !httpCon.getContentType().contains("charset=UTF-8")) {
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
			httpCon.disconnect();
		}
	}

}
