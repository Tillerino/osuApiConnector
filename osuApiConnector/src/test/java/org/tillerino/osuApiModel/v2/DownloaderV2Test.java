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
import java.net.URI;
import java.util.List;
import lombok.Getter;
import org.junit.Test;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.tillerino.osuApiModel.*;

public class DownloaderV2Test extends AbstractMockServerV2Test {

    private final DownloaderV2 invalidTokenRealUriDownloader =
            new DownloaderV2(TokenHelper.TokenCache.constant("fake"));

    // Static so that a token is requested once for all tests.
    // Lazy so that only those tests fail which require the downloader.
    @Getter(lazy = true)
    private static final DownloaderV2 prodDownloader = new DownloaderV2();

    @Test
    public void testFormURI() throws IOException {
        assertEquals(
                URI.create("https://osu.ppy.sh/api/v2/verb?parameter=%2F"),
                invalidTokenRealUriDownloader.formURI("verb?parameter={value}", "{value}", "/"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormURIWrongArgNumber() throws IOException {
        invalidTokenRealUriDownloader.formURI("verb", "parameterWithoutValue");
    }

    @Test
    public void testNoValidKey() throws IOException {
        try {
            invalidTokenRealUriDownloader.getBeatmap(75, 0L, OsuApiBeatmap.class);
            fail("we expect an exception");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("401"));
        }
    }

    @Test
    public void testInvalidVerb() throws IOException {

        assertThatThrownBy(() ->
                        DownloaderV2.downloadDirect(invalidTokenRealUriDownloader.formURI("verb"), "1234", "GET", null))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("response code 401");
    }

    @Test
    public void testBeatmapNotFound() throws IOException {
        mockServer
                .when(new HttpRequest()
                        .withPath("/api/v2/beatmaps/1")
                        .withHeader(new Header("Authorization", "Bearer fake-token")))
                .respond(new HttpResponse()
                        .withBody("{ }")
                        .withHeader("Content-type", "application/json;charset=UTF-8"));

        mockServer
                .when(new HttpRequest()
                        .withPath("/api/v2/beatmaps/1/attributes")
                        .withHeader(new Header("Authorization", "Bearer fake-token")))
                .respond(new HttpResponse()
                        .withBody("{ }")
                        .withHeader("Content-type", "application/json;charset=UTF-8"));

        assertNull(downloader.getBeatmap(1, 0L, OsuApiBeatmap.class));
    }

    @Test
    public void testGetBeatmap() throws Exception {
        OsuApiBeatmap nomod = getProdDownloader().getBeatmap(75, 0L, OsuApiBeatmap.class);
        assertThat(nomod).isNotNull();

        // also get for DT to check that mods work
        OsuApiBeatmap dt = getProdDownloader().getBeatmap(75, Mods.getMask(Mods.DoubleTime), OsuApiBeatmap.class);
        assertThat(dt).isNotNull();

        assertThat(dt.getSpeedDifficulty()).isGreaterThan(nomod.getSpeedDifficulty());
    }

    @Test
    public void testGetBeatmapTop() throws Exception {
        assertThat(getProdDownloader().getBeatmapTop(53, 0, OsuApiScore.class)).hasSize(50);
    }

    @Test
    public void testGetBeatmapTopNomod() throws Exception {
        int mods = 0;
        String[] modsArray = bitwiseToModsArray(mods).toArray(new String[0]);

        final List<OsuApiScore> beatmapTop = getProdDownloader().getBeatmapTop(53, 0, modsArray, OsuApiScore.class);

        for (OsuApiScore osuApiScore : beatmapTop) {
            int apiMods = Math.toIntExact(osuApiScore.getMods());
            assertTrue(apiMods == 0
                    || apiMods == 32
                    || apiMods == 16384); // nomod scores always come with PF and SD scores
        }
    }

    @Test
    public void testGetBeatmapTopHDDT() throws Exception {
        int mods = 72;
        String[] modsArray = bitwiseToModsArray(mods).toArray(new String[0]);

        final List<OsuApiScore> beatmapTop = getProdDownloader().getBeatmapTop(53, 0, modsArray, OsuApiScore.class);

        for (OsuApiScore osuApiScore : beatmapTop) {
            int apiMods = Math.toIntExact(osuApiScore.getMods());
          assertEquals(8, (apiMods & 8));
          assertTrue((apiMods & 64) == 64 || (apiMods & 512) == 512);
        }
    }

    @Test
    public void testGetUser() throws Exception {
        getProdDownloader().getUser("Tillerino", GameModes.OSU, OsuApiUser.class);
    }

    @Test
    public void testGetUserRecent() throws Exception {
        getProdDownloader().getUserRecent(34226059, GameModes.OSU, OsuApiScore.class);
    }

    @Test
    public void testGetScore() throws Exception {
        final OsuApiScore score = getProdDownloader().getScore(2070907, 239265, GameModes.OSU, OsuApiScore.class);
        assertNotNull(score.getPp());
    }

    @Test
    public void testDiffStuff() throws Exception {
        mockServer
                .when(new HttpRequest().withMethod("GET").withPath("/api/v2/beatmaps/129891"))
                .respond(
                        new HttpResponse()
                                .withHeader("Content-Type", "application/json;charset=UTF-8")
                                .withBody(
                                        "{\"beatmapset_id\":39804,\"difficulty_rating\":7.45,\"id\":129891,\"mode\":\"osu\",\"status\":\"approved\",\"total_length\":258,\"user_id\":87065,\"version\":\"FOUR DIMENSIONS\",\"accuracy\":8,\"ar\":9,\"bpm\":222.22,\"convert\":false,\"count_circles\":1646,\"count_sliders\":335,\"count_spinners\":2,\"cs\":4,\"deleted_at\":null,\"drain\":6,\"hit_length\":226,\"is_scoreable\":true,\"last_updated\":\"2014-05-18T17:22:14Z\",\"mode_int\":0,\"passcount\":319328,\"playcount\":10023935,\"ranked\":2,\"url\":\"https://osu.ppy.sh/beatmaps/129891\",\"checksum\":\"da8aae79c8f3306b5d65ec951874a7fb\",\"beatmapset\":{\"artist\":\"xi\",\"artist_unicode\":\"xi\",\"covers\":{\"cover\":\"https://assets.ppy.sh/beatmaps/39804/covers/cover.jpg?1650612317\",\"cover@2x\":\"https://assets.ppy.sh/beatmaps/39804/covers/cover@2x.jpg?1650612317\",\"card\":\"https://assets.ppy.sh/beatmaps/39804/covers/card.jpg?1650612317\",\"card@2x\":\"https://assets.ppy.sh/beatmaps/39804/covers/card@2x.jpg?1650612317\",\"list\":\"https://assets.ppy.sh/beatmaps/39804/covers/list.jpg?1650612317\",\"list@2x\":\"https://assets.ppy.sh/beatmaps/39804/covers/list@2x.jpg?1650612317\",\"slimcover\":\"https://assets.ppy.sh/beatmaps/39804/covers/slimcover.jpg?1650612317\",\"slimcover@2x\":\"https://assets.ppy.sh/beatmaps/39804/covers/slimcover@2x.jpg?1650612317\"},\"creator\":\"Nakagawa-Kanon\",\"favourite_count\":8150,\"genre_id\":2,\"hype\":null,\"id\":39804,\"language_id\":5,\"nsfw\":false,\"offset\":0,\"play_count\":21601856,\"preview_url\":\"//b.ppy.sh/preview/39804.mp3\",\"source\":\"BMS\",\"spotlight\":false,\"status\":\"approved\",\"title\":\"FREEDOM DiVE\",\"title_unicode\":\"FREEDOM DiVE\",\"track_id\":5864,\"user_id\":87065,\"video\":false,\"bpm\":222.22,\"can_be_hyped\":false,\"deleted_at\":null,\"discussion_enabled\":true,\"discussion_locked\":false,\"is_scoreable\":true,\"last_updated\":\"2012-06-23T03:19:39Z\",\"legacy_thread_url\":\"https://osu.ppy.sh/community/forums/topics/66915\",\"nominations_summary\":{\"current\":0,\"eligible_main_rulesets\":[\"taiko\"],\"required_meta\":{\"main_ruleset\":2,\"non_main_ruleset\":1}},\"ranked\":2,\"ranked_date\":\"2012-06-23T16:42:35Z\",\"rating\":9.5622,\"storyboard\":false,\"submitted_date\":\"2011-11-14T16:46:32Z\",\"tags\":\"parousia onosakihito kirisaki_hayashi\",\"availability\":{\"download_disabled\":false,\"more_information\":null},\"ratings\":[0,134,8,8,16,22,25,68,116,325,4744]},\"current_user_playcount\":0,\"failtimes\":{\"fail\":[],\"exit\":[]},\"max_combo\":2385,\"owners\":[{\"id\":87065,\"username\":\"Nakagawa-Kanon\"}]}"));

        mockServer
                .when(new HttpRequest().withMethod("POST").withPath("/api/v2/beatmaps/129891/attributes"))
                .respond(
                        new HttpResponse()
                                .withHeader("Content-Type", "application/json;charset=UTF-8")
                                .withBody(
                                        "{\"attributes\":{\"star_rating\":7.453050136566162,\"max_combo\":2385,\"aim_difficulty\":3.403049945831299,\"aim_difficult_slider_count\":151.7519989013672,\"speed_difficulty\":3.6965999603271484,\"speed_note_count\":1299.47998046875,\"slider_factor\":0.9964190125465393,\"aim_difficult_strain_count\":322.5840148925781,\"speed_difficult_strain_count\":598.8939819335938}}"));

        assertThat(downloader.getBeatmap(129891, 0L, OsuApiBeatmap.class))
                .hasFieldOrPropertyWithValue("aimDifficulty", 3.403049945831299)
                .hasFieldOrPropertyWithValue("speedDifficulty", 3.6965999603271484);
    }

    @Test
    public void modsSpecificBeatmapCanBePulled() throws Exception {
        mockServer
                .when(request("/api/v2/beatmaps/123"))
                // we can return a missing beatmap, we just want to know that the query is right
                .respond(response().withBody("{}", MediaType.JSON_UTF_8));

        mockServer
                .when(request("/api/v2/beatmaps/123/attributes"))
                // we can return a missing beatmap, we just want to know that the query is right
                .respond(response().withBody("{}", MediaType.JSON_UTF_8));

        assertThat(downloader.getBeatmap(123, 0L, OsuApiBeatmap.class)).isNull();

        mockServer.verify(request("/api/v2/beatmaps/123"));
        mockServer.verify(request("/api/v2/beatmaps/123/attributes"));
    }

    static List<String> bitwiseToModsArray(int bitwise) {
        if (bitwise == 0) {
            return List.of("NM");
        }

        return Mods.getMods(bitwise).stream().map(Mods::getShortName).toList();
    }
}
