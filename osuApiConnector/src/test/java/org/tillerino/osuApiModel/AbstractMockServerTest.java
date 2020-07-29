package org.tillerino.osuApiModel;

import java.net.URL;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;

public abstract class AbstractMockServerTest {
	@ClassRule
	public static final MockServerRule mockServerRule = new MockServerRule(DownloaderTest.class);

	protected static MockServerClient mockServer;

	@Rule
	public final TestWatcher watcher = new TestWatcher() {
		protected void failed(Throwable e, Description description) {
			System.err.println(mockServer.retrieveLogMessages(null));
		}

		protected void finished(Description description) {
			mockServer.reset();
		}
	};

	protected Downloader downloader;

	@Before
	public void setUp() throws Exception {
		downloader = new Downloader(new URL("http://localhost:" + mockServerRule.getPort() + "/"), "key");
	}
}
