package org.tillerino.osuApiModel;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;


public class DownloaderTest {
	@Test
	public void testFormURL() throws MalformedURLException {
		assertEquals(new URL("http://osu.ppy.sh/api/verb?k=key&parameter=value"), new Downloader("key").formURL("verb", "parameter", "value"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFormURLWrongArgNumber() throws MalformedURLException {
		new Downloader("key").formURL("verb", "parameterWithoutValue");
	}
	
	@Test
	public void testNoValidKey() throws IOException {
		String responseString = Downloader.downloadDirect(new Downloader("wrongKey").formURL(Downloader.GET_BEATMAPS));
		
		assertEquals(Downloader.INVALID_API_KEY, responseString);
	}
	
	@Test(expected=IOException.class)
	public void testInvalidVerb() throws IOException {
		try {
			Downloader.downloadDirect(new Downloader("wrongKey").formURL("verb"));
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
}
