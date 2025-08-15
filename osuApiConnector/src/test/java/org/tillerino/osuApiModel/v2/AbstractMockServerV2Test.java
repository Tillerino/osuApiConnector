package org.tillerino.osuApiModel.v2;

import java.net.URI;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;

public abstract class AbstractMockServerV2Test {
    @ClassRule
    public static final MockServerRule mockServerRule = new MockServerRule(DownloaderV2Test.class);

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

    protected DownloaderV2 downloader;

    @Before
    public void setUp() throws Exception {
        downloader = new DownloaderV2(
                URI.create("http://localhost:" + mockServerRule.getPort() + "/"),
                TokenHelper.TokenCache.constant("fake-token"));
    }
}
