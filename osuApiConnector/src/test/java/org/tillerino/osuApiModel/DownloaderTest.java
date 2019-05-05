package org.tillerino.osuApiModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class DownloaderTest {
	public MockServerRule mockServerRule = new MockServerRule(this);

	private MockServerClient mockServer;

	public final TestWatcher watcher = new TestWatcher() {
		protected void failed(Throwable e, Description description) {
			System.err.println(mockServer.retrieveLogMessages(null));
		};
	};

	@Rule
	public RuleChain rules = RuleChain.outerRule(mockServerRule).around(watcher);

	@Test
	public void testFormURL() throws IOException {
		assertEquals(new URL("http://osu.ppy.sh/api/verb?k=key&parameter=value"), new Downloader("key").formURL(true, "verb", "parameter", "value"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFormURLWrongArgNumber() throws IOException {
		new Downloader("key").formURL(true, "verb", "parameterWithoutValue");
	}
	
	@Test
	public void testNoValidKey() throws IOException {
		try {
			Downloader.downloadDirect(new Downloader("wrongKey").formURL(true, Downloader.GET_BEATMAPS));
			fail("we expect an exception");
		} catch (IOException e) {
			assertTrue(e.getMessage().contains("401"));
		}
	}
	
	@Test(expected=IOException.class)
	public void testInvalidVerb() throws IOException {
		try {
			Downloader.downloadDirect(new Downloader("wrongKey").formURL(true, "verb"));
		} catch (IOException e) {
			assertTrue(e.getMessage().contains("response code 301"));
			throw e;
		}
	}
	
	/**
	 * For this test to succeed, there has to be a resource "osuapikey" or
	 * system property "osuapikey" containing a valid api key, which is the
	 * recommended way of using the {@link Downloader}
	 */
	@Test
	public void testImplicitApiKey() {
		new Downloader();
	}
	
	@Test
	public void testBeatmapNotFound() throws IOException {
		mockServer.when(new HttpRequest()
				.withPath("/get_beatmaps")
				.withQueryStringParameter("k", "key")
				.withQueryStringParameter("b", "1")
				.withHeader(new Header(".*", ".*")))
		.respond(new HttpResponse()
				.withBody("[ ]")
				.withHeader("Content-type", "application/json;charset=UTF-8"));

		Downloader downloader = new Downloader(new URL("http://localhost:" + mockServerRule.getPort() + "/"), "key");

		assertNull(downloader.getBeatmap(1, OsuApiBeatmap.class));
	}

	@Test
	public void testGetBeatmapTop() throws Exception {
		Downloader downloader = new Downloader();
		
		downloader.getBeatmapTop(53, 0, OsuApiScore.class);
	}

	@Test
	public void testGetBeatmapTopNomod() throws Exception {
		Downloader downloader = new Downloader();
		
		final List<OsuApiScore> beatmapTop = downloader.getBeatmapTop(53, 0, OsuApiScore.class, 0);
		
		for (OsuApiScore osuApiScore : beatmapTop) {
			assertEquals(0, osuApiScore.getMods());
		}
	}

	@Test
	public void testGetBeatmapTopDT() throws Exception {
		Downloader downloader = new Downloader();
		
		final List<OsuApiScore> beatmapTop = downloader.getBeatmapTop(53, 0, OsuApiScore.class, 64);
		
		for (OsuApiScore osuApiScore : beatmapTop) {
			assertEquals(64, osuApiScore.getMods());
		}
	}
	
	@Test
	public void testGetUser() throws Exception {
		Downloader downloader = new Downloader();
		
		downloader.getUser("Tillerino", GameModes.OSU, OsuApiUser.class);
	}

	@Test
	public void testGetUserRecent() throws Exception {
		Downloader downloader = new Downloader();
		
		downloader.getUserRecent(2070907, GameModes.OSU, OsuApiScore.class);
	}

	@Test
	public void testGetScore() throws Exception {
		Downloader downloader = new Downloader();
		
		final OsuApiScore score = downloader.getScore(2070907, 239265, GameModes.OSU, OsuApiScore.class);
		assertNotNull(score.getPp());
	}
}
