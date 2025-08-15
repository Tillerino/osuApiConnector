package org.tillerino.osuApiModel.v2;

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
import org.tillerino.osuApiModel.*;

public class DownloaderV2Test extends AbstractMockServerV2Test {
    @Test
    public void testFormURL() throws IOException {
        assertEquals(
                new URL("https://osu.ppy.sh/api/v2/verb?parameter=value"),
                new DownloaderV2("key").formURL("verb", "parameter", "value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormURLWrongArgNumber() throws IOException {
        new DownloaderV2("key").formURL("verb", "parameterWithoutValue");
    }

    @Test
    public void testNoValidKey() throws IOException {
        try {
            DownloaderV2.downloadDirect(new DownloaderV2("wrongKey").formURL(DownloaderV2.GET_BEATMAPS), "1234", "GET");
            fail("we expect an exception");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("401"));
        }
    }

    @Test
    public void testInvalidVerb() throws IOException {

        assertThatThrownBy(
                        () -> DownloaderV2.downloadDirect(new DownloaderV2("wrongKey").formURL("verb"), "1234", "GET"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("response code 401");
    }

    /**
     * For this test to succeed, there has to be a resource "osuapikey" or
     * system property "osuapikey" containing a valid api key, which is the
     * recommended way of using the {@link DownloaderV2}
     */
    @Test
    public void testImplicitApiKey() {
        new DownloaderV2();
    }

    @Test
    public void testBeatmapNotFound() throws IOException {
        TokenHelper.setTestTokenOverride("fake-token");

        mockServer
                .when(new HttpRequest()
                        .withPath("/beatmaps/1")
                        .withHeader(new Header("Authorization", "Bearer fake-token")))
                .respond(new HttpResponse()
                        .withBody("{ }")
                        .withHeader("Content-type", "application/json;charset=UTF-8"));

        mockServer
                .when(new HttpRequest()
                        .withPath("/beatmaps/1/attributes")
                        .withHeader(new Header("Authorization", "Bearer fake-token")))
                .respond(new HttpResponse()
                        .withBody("{ }")
                        .withHeader("Content-type", "application/json;charset=UTF-8"));

        assertNull(downloader.getBeatmap(1));

        TokenHelper.setTestTokenOverride(null);
    }

    @Test
    public void testGetBeatmap() throws Exception {
        DownloaderV2 downloader = new DownloaderV2();

        downloader.getBeatmap(75);
    }

    @Test
    public void testGetBeatmapTop() throws Exception {
        DownloaderV2 downloader = new DownloaderV2();

        downloader.getBeatmapTop(53, 0);
    }

    @Test
    public void testGetBeatmapTopNomod() throws Exception {
        DownloaderV2 downloader = new DownloaderV2();

        int mods = 0;
        String[] modsArray = bitwiseToModsArray(mods).toArray(new String[0]);

        final List<OsuApiScore> beatmapTop = downloader.getBeatmapTop(53, 0, modsArray);

        for (OsuApiScore osuApiScore : beatmapTop) {
            int apiMods = Math.toIntExact(osuApiScore.getMods());
            assertTrue(apiMods == 0
                    || apiMods == 32
                    || apiMods == 16384); // nomod scores always come with PF and SD scores
        }
    }

    @Test
    public void testGetBeatmapTopHDDT() throws Exception {
        DownloaderV2 downloader = new DownloaderV2();

        int mods = 72;
        String[] modsArray = bitwiseToModsArray(mods).toArray(new String[0]);

        final List<OsuApiScore> beatmapTop = downloader.getBeatmapTop(53, 0, modsArray);

        for (OsuApiScore osuApiScore : beatmapTop) {
            int apiMods = Math.toIntExact(osuApiScore.getMods());
            assertTrue(apiMods == 72 || apiMods == 520); // DT+HD or NC+HD
        }
    }

    @Test
    public void testGetUser() throws Exception {
        DownloaderV2 downloader = new DownloaderV2();

        downloader.getUser("Tillerino", GameModes.OSU);
    }

    @Test
    public void testGetUserRecent() throws Exception {
        DownloaderV2 downloader = new DownloaderV2();

        downloader.getUserRecent(34226059, GameModes.OSU);
    }

    @Test
    public void testGetScore() throws Exception {
        DownloaderV2 downloader = new DownloaderV2();

        final OsuApiScore score = downloader.getScore(2070907, 239265, GameModes.OSU);
        assertNotNull(score.getPp());
    }

    @Test
    public void testDiffStuff() throws Exception {
        mockServer
                .when(new HttpRequest().withMethod("GET").withPath("/beatmaps/129891"))
                .respond(
                        new HttpResponse()
                                .withHeader("Content-Type", "application/json;charset=UTF-8")
                                .withBody(
                                        "{\"beatmapset_id\":39804,\"difficulty_rating\":7.45,\"id\":129891,\"mode\":\"osu\",\"status\":\"approved\",\"total_length\":258,\"user_id\":87065,\"version\":\"FOUR DIMENSIONS\",\"accuracy\":8,\"ar\":9,\"bpm\":222.22,\"convert\":false,\"count_circles\":1646,\"count_sliders\":335,\"count_spinners\":2,\"cs\":4,\"deleted_at\":null,\"drain\":6,\"hit_length\":226,\"is_scoreable\":true,\"last_updated\":\"2014-05-18T17:22:14Z\",\"mode_int\":0,\"passcount\":319328,\"playcount\":10023935,\"ranked\":2,\"url\":\"https://osu.ppy.sh/beatmaps/129891\",\"checksum\":\"da8aae79c8f3306b5d65ec951874a7fb\",\"beatmapset\":{\"artist\":\"xi\",\"artist_unicode\":\"xi\",\"covers\":{\"cover\":\"https://assets.ppy.sh/beatmaps/39804/covers/cover.jpg?1650612317\",\"cover@2x\":\"https://assets.ppy.sh/beatmaps/39804/covers/cover@2x.jpg?1650612317\",\"card\":\"https://assets.ppy.sh/beatmaps/39804/covers/card.jpg?1650612317\",\"card@2x\":\"https://assets.ppy.sh/beatmaps/39804/covers/card@2x.jpg?1650612317\",\"list\":\"https://assets.ppy.sh/beatmaps/39804/covers/list.jpg?1650612317\",\"list@2x\":\"https://assets.ppy.sh/beatmaps/39804/covers/list@2x.jpg?1650612317\",\"slimcover\":\"https://assets.ppy.sh/beatmaps/39804/covers/slimcover.jpg?1650612317\",\"slimcover@2x\":\"https://assets.ppy.sh/beatmaps/39804/covers/slimcover@2x.jpg?1650612317\"},\"creator\":\"Nakagawa-Kanon\",\"favourite_count\":8150,\"genre_id\":2,\"hype\":null,\"id\":39804,\"language_id\":5,\"nsfw\":false,\"offset\":0,\"play_count\":21601856,\"preview_url\":\"//b.ppy.sh/preview/39804.mp3\",\"source\":\"BMS\",\"spotlight\":false,\"status\":\"approved\",\"title\":\"FREEDOM DiVE\",\"title_unicode\":\"FREEDOM DiVE\",\"track_id\":5864,\"user_id\":87065,\"video\":false,\"bpm\":222.22,\"can_be_hyped\":false,\"deleted_at\":null,\"discussion_enabled\":true,\"discussion_locked\":false,\"is_scoreable\":true,\"last_updated\":\"2012-06-23T03:19:39Z\",\"legacy_thread_url\":\"https://osu.ppy.sh/community/forums/topics/66915\",\"nominations_summary\":{\"current\":0,\"eligible_main_rulesets\":[\"taiko\"],\"required_meta\":{\"main_ruleset\":2,\"non_main_ruleset\":1}},\"ranked\":2,\"ranked_date\":\"2012-06-23T16:42:35Z\",\"rating\":9.5622,\"storyboard\":false,\"submitted_date\":\"2011-11-14T16:46:32Z\",\"tags\":\"parousia onosakihito kirisaki_hayashi\",\"availability\":{\"download_disabled\":false,\"more_information\":null},\"ratings\":[0,134,8,8,16,22,25,68,116,325,4744]},\"current_user_playcount\":0,\"failtimes\":{\"fail\":[],\"exit\":[]},\"max_combo\":2385,\"owners\":[{\"id\":87065,\"username\":\"Nakagawa-Kanon\"}]}"));

        mockServer
                .when(new HttpRequest().withMethod("POST").withPath("/beatmaps/129891/attributes"))
                .respond(
                        new HttpResponse()
                                .withHeader("Content-Type", "application/json;charset=UTF-8")
                                .withBody(
                                        "{\"attributes\":{\"star_rating\":7.453050136566162,\"max_combo\":2385,\"aim_difficulty\":3.403049945831299,\"aim_difficult_slider_count\":151.7519989013672,\"speed_difficulty\":3.6965999603271484,\"speed_note_count\":1299.47998046875,\"slider_factor\":0.9964190125465393,\"aim_difficult_strain_count\":322.5840148925781,\"speed_difficult_strain_count\":598.8939819335938}}"));

        assertThat(downloader.getBeatmap(129891))
                .hasFieldOrPropertyWithValue("aimDifficulty", 3.403049945831299)
                .hasFieldOrPropertyWithValue("speedDifficulty", 3.6965999603271484);
    }

    @Test
    public void modsSpecificBeatmapCanBePulled() throws Exception {
        mockServer
                .when(request("/beatmaps/123"))
                // we can return a missing beatmap, we just want to know that the query is right
                .respond(response().withBody("{}", MediaType.JSON_UTF_8));

        mockServer
                .when(request("/beatmaps/123/attributes"))
                // we can return a missing beatmap, we just want to know that the query is right
                .respond(response().withBody("{}", MediaType.JSON_UTF_8));

        assertThat(downloader.getBeatmap(123)).isNull();

        mockServer.verify(request("/beatmaps/123"));
        mockServer.verify(request("/beatmaps/123/attributes"));
    }

    static List<String> bitwiseToModsArray(int bitwise) {
        if (bitwise == 0) {
            return List.of("NM");
        }

        return Mods.getMods(bitwise).stream().map(Mods::getShortName).toList();
    }
}
