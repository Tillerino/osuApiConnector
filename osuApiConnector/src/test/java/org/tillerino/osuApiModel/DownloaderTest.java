package org.tillerino.osuApiModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import com.google.common.net.MediaType;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.junit.Test;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class DownloaderTest extends AbstractMockServerTest {
    @Test
    public void testFormURL() throws IOException {
        assertEquals(
                new URL("https://osu.ppy.sh/api/verb?k=key&parameter=value"),
                new Downloader("key").formURL(true, "verb", "parameter", "value"));
    }

    @Test(expected = IllegalArgumentException.class)
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

    @Test
    public void testInvalidVerb() throws IOException {
        assertThatThrownBy(() -> Downloader.downloadDirect(new Downloader("wrongKey").formURL(true, "verb")))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("response code 302");
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
        mockServer
                .when(new HttpRequest()
                        .withPath("/get_beatmaps")
                        .withQueryStringParameter("k", "key")
                        .withQueryStringParameter("b", "1")
                        .withHeader(new Header(".*", ".*")))
                .respond(new HttpResponse()
                        .withBody("[ ]")
                        .withHeader("Content-type", "application/json;charset=UTF-8"));

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

    @Test
    public void testDiffStuff() throws Exception {
        mockServer
                .when(new HttpRequest()
                        .withPath("/get_beatmaps")
                        .withQueryStringParameter("k", "key")
                        .withQueryStringParameter("b", "129891")
                        .withHeader(new Header(".*", ".*")))
                .respond(new HttpResponse()
                        .withBody(
                                "[{\"beatmapset_id\":\"39804\",\"beatmap_id\":\"129891\",\"approved\":\"2\",\"total_length\":\"258\",\"hit_length\":\"226\",\"version\":\"FOUR DIMENSIONS\",\"file_md5\":\"da8aae79c8f3306b5d65ec951874a7fb\",\"diff_size\":\"4\",\"diff_overall\":\"8\",\"diff_approach\":\"9\",\"diff_drain\":\"6\",\"mode\":\"0\",\"approved_date\":\"2012-06-23 16:42:35\",\"last_update\":\"2012-06-23 03:19:39\",\"artist\":\"xi\",\"title\":\"FREEDOM DiVE\",\"creator\":\"Nakagawa-Kanon\",\"creator_id\":\"87065\",\"bpm\":\"222.22\",\"source\":\"BMS\",\"tags\":\"parousia onosakihito kirisaki_hayashi\",\"genre_id\":\"2\",\"language_id\":\"5\",\"favourite_count\":\"2761\",\"playcount\":\"5316179\",\"passcount\":\"208115\",\"max_combo\":\"2385\",\"diff_aim\":\"3.3820393085479736\",\"diff_speed\":\"3.7348108291625977\",\"difficultyrating\":\"7.293235778808594\"}]")
                        .withHeader("Content-type", "application/json;charset=UTF-8"));

        assertThat(downloader.getBeatmap(129891, OsuApiBeatmap.class))
                .hasFieldOrPropertyWithValue("aimDifficulty", 3.3820393085479736)
                .hasFieldOrPropertyWithValue("speedDifficulty", 3.7348108291625977);
    }

    @Test
    public void modsSpecificBeatmapCanBePulled() throws Exception {
        mockServer
                .when(request("/get_beatmaps")
                        .withQueryStringParameter("k", "key")
                        .withQueryStringParameter("b", "123")
                        .withQueryStringParameter("mods", "16"))
                // we can return a missing beatmap, we just want to know that the query is right
                .respond(response().withBody("[]", MediaType.JSON_UTF_8));

        assertThat(downloader.getBeatmap(123, 16, OsuApiBeatmap.class)).isNull();

        mockServer.verify(request("/get_beatmaps")
                .withQueryStringParameter("k", "key")
                .withQueryStringParameter("b", "123")
                .withQueryStringParameter("mods", "16"));
    }
}
