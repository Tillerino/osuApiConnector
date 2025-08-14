package org.tillerino.osuApiModel.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.tillerino.osuApiModel.Mods.*;

import com.google.common.net.MediaType;
import java.io.IOException;
import org.junit.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;

public class OsuApiBeatmapV2Test extends AbstractMockServerV2Test {
    private OsuApiBeatmap expectedDiscoPrince() {
        OsuApiBeatmap expected = new OsuApiBeatmap();

        expected.setBeatmapId(75);
        expected.setSetId(1);
        expected.setArtist("Kenji Ninuma");
        expected.setTitle("DISCO PRINCE");
        expected.setVersion("Normal");
        expected.setCreator("peppy");
        expected.setSource(null);
        expected.setApproved(1);
        expected.setApprovedDate(1191692791000L);
        expected.setLastUpdate(1718293407000L);
        expected.setBpm(120);
        expected.setStarDifficulty(2.58);
        expected.setOverallDifficulty(6);
        expected.setCircleSize(4);
        expected.setApproachRate(6);
        expected.setHealthDrain(6);
        expected.setHitLength(109);
        expected.setTotalLength(142);
        expected.setMode(0);
        expected.setFileMd5("233f55099932d0696a3ef192041bc30d");
        expected.setMaxCombo(314);
        expected.setAimDifficulty(1.3055499792099);
        expected.setSpeedDifficulty(1.1394100189208984);
        expected.setTags("katamari");
        expected.setCreatorId(2);
        expected.setGenreId(2);
        expected.setLanguageId(3);
        return expected;
    }

    @Test
    public void testRegressionDownload() throws IOException {
        OsuApiBeatmap expected = expectedDiscoPrince();

        OsuApiBeatmap downloaded = new DownloaderV2().getBeatmap(75);
        assertNotNull(downloaded);

        expected.setPlayCount(downloaded.getPlayCount());
        expected.setPassCount(downloaded.getPassCount());
        expected.setFavouriteCount(downloaded.getFavouriteCount());

        assertEquals(expected, downloaded);
    }

    @Test
    public void testRegressionFixed() throws IOException {
        String beatmapInfo =
                "{\"beatmapset_id\":1,\"difficulty_rating\":2.58,\"id\":75,\"mode\":\"osu\",\"status\":\"ranked\",\"total_length\":142,\"user_id\":2,\"version\":\"Normal\",\"accuracy\":6,\"ar\":6,\"bpm\":120,\"convert\":false,\"count_circles\":160,\"count_sliders\":30,\"count_spinners\":4,\"cs\":4,\"deleted_at\":null,\"drain\":6,\"hit_length\":109,\"is_scoreable\":true,\"last_updated\":\"2024-06-13T15:43:27Z\",\"mode_int\":0,\"passcount\":90473,\"playcount\":727763,\"ranked\":1,\"url\":\"https://osu.ppy.sh/beatmaps/75\",\"checksum\":\"233f55099932d0696a3ef192041bc30d\",\"beatmapset\":{\"artist\":\"Kenji Ninuma\",\"artist_unicode\":\"Kenji Ninuma\",\"covers\":{\"cover\":\"https://assets.ppy.sh/beatmaps/1/covers/cover.jpg?1732202342\",\"cover@2x\":\"https://assets.ppy.sh/beatmaps/1/covers/cover@2x.jpg?1732202342\",\"card\":\"https://assets.ppy.sh/beatmaps/1/covers/card.jpg?1732202342\",\"card@2x\":\"https://assets.ppy.sh/beatmaps/1/covers/card@2x.jpg?1732202342\",\"list\":\"https://assets.ppy.sh/beatmaps/1/covers/list.jpg?1732202342\",\"list@2x\":\"https://assets.ppy.sh/beatmaps/1/covers/list@2x.jpg?1732202342\",\"slimcover\":\"https://assets.ppy.sh/beatmaps/1/covers/slimcover.jpg?1732202342\",\"slimcover@2x\":\"https://assets.ppy.sh/beatmaps/1/covers/slimcover@2x.jpg?1732202342\"},\"creator\":\"peppy\",\"favourite_count\":1608,\"genre_id\":2,\"hype\":null,\"id\":1,\"language_id\":3,\"nsfw\":false,\"offset\":0,\"play_count\":727763,\"preview_url\":\"//b.ppy.sh/preview/1.mp3\",\"source\":\"\",\"spotlight\":false,\"status\":\"ranked\",\"title\":\"DISCO PRINCE\",\"title_unicode\":\"DISCO PRINCE\",\"track_id\":null,\"user_id\":2,\"video\":false,\"bpm\":120,\"can_be_hyped\":false,\"deleted_at\":null,\"discussion_enabled\":true,\"discussion_locked\":false,\"is_scoreable\":true,\"last_updated\":\"2007-10-06T17:46:31Z\",\"legacy_thread_url\":\"https://osu.ppy.sh/community/forums/topics/213\",\"nominations_summary\":{\"current\":0,\"eligible_main_rulesets\":[\"osu\"],\"required_meta\":{\"main_ruleset\":2,\"non_main_ruleset\":1}},\"ranked\":1,\"ranked_date\":\"2007-10-06T17:46:31Z\",\"rating\":8.33215,\"storyboard\":false,\"submitted_date\":\"2007-10-06T17:46:31Z\",\"tags\":\"katamari\",\"availability\":{\"download_disabled\":false,\"more_information\":null},\"ratings\":[0,155,15,29,38,49,78,119,149,136,1207]},\"current_user_playcount\":0,\"failtimes\":{\"fail\":[2,0,1,2,1,3,0,335,824,500,922,1174,874,1024,1328,4037,11899,7962,5831,5005,5423,3718,360,2025,10,0,2,1,2,1,5,2,1,6787,14875,10051,11210,8909,13982,12163,10536,20369,19245,19339,14786,1085,573,2,1,2,2,92,862,621,1462,9549,4891,2625,1215,630,1332,3330,2959,1379,843,1327,4539,1185,109,572,1673,1108,632,618,135,0,1,0,211,627,2868,1767,1039,2221,2963,995,878,351,399,294,331,1691,2522,1161,1264,1004,263,465,704,478],\"exit\":[0,0,0,0,0,1,0,36,423,430,2607,12266,9208,7355,4504,3499,10719,10382,5096,4725,3367,2727,2993,5030,26533,18632,8310,4648,2902,3308,1522,1127,630,2484,9335,6253,3988,2732,6040,7294,7539,8909,7753,3368,3915,5858,9640,10054,4449,2417,1057,315,1498,1604,538,1432,1751,2233,2839,1337,632,2084,2067,2122,1383,957,880,2719,981,1683,1393,815,1057,367,1927,1971,890,329,73,164,499,1027,895,612,1558,984,428,375,307,360,209,423,1161,749,458,597,563,281,378,2266]},\"max_combo\":314,\"owners\":[{\"id\":2,\"username\":\"peppy\"}]}";
        mockServer
                .when(HttpRequest.request("/beatmaps/75"))
                .respond(HttpResponse.response().withBody(beatmapInfo, MediaType.JSON_UTF_8));

        String beatmapAttributes =
                "{\"attributes\":{\"star_rating\":2.5756900310516357,\"max_combo\":314,\"aim_difficulty\":1.3055499792099,\"aim_difficult_slider_count\":10.689000129699707,\"speed_difficulty\":1.1394100189208984,\"speed_note_count\":83.49919891357422,\"slider_factor\":0.9829580187797546,\"aim_difficult_strain_count\":30.505800247192383,\"speed_difficult_strain_count\":36.56919860839844}}";
        mockServer
                .when(HttpRequest.request("/beatmaps/75/attributes"))
                .respond(HttpResponse.response().withBody(beatmapAttributes, MediaType.JSON_UTF_8));

        OsuApiBeatmap expected = expectedDiscoPrince();
        OsuApiBeatmap downloaded = downloader.getBeatmap(75);
        assertNotNull(downloaded);

        expected.setPlayCount(downloaded.getPlayCount());
        expected.setPassCount(downloaded.getPassCount());
        expected.setFavouriteCount(downloaded.getFavouriteCount());

        assertEquals(expected, downloaded);
    }

    @Test
    public void testCalcOd() throws Exception {
        assertEquals(-0.42, OsuApiBeatmap.calcOd(6, getMask(HalfTime, Easy)), 1E-2);
        assertEquals(4.92, OsuApiBeatmap.calcOd(7, getMask(HalfTime)), 1E-2);
        assertEquals(8.92, OsuApiBeatmap.calcOd(10, getMask(HalfTime, HardRock)), 1E-2);
        assertEquals(5.75, OsuApiBeatmap.calcOd(4, getMask(DoubleTime, Easy)), 1E-2);
        assertEquals(9.75, OsuApiBeatmap.calcOd(8, getMask(DoubleTime)), 2E-2);
        assertEquals(10.42, OsuApiBeatmap.calcOd(9, getMask(DoubleTime)), 2E-2);
        assertEquals(11.08, OsuApiBeatmap.calcOd(10, getMask(DoubleTime, HardRock)), 1E-2);
    }

    @Test
    public void testMsToAr() {
        assertEquals(-7.5, OsuApiBeatmap.msToAr(2700), 1E-15);
        assertEquals(0, OsuApiBeatmap.msToAr(1800), 1E-15);
        assertEquals(2, OsuApiBeatmap.msToAr(1560), 1E-15);
        assertEquals(5, OsuApiBeatmap.msToAr(1200), 1E-15);
        assertEquals(7, OsuApiBeatmap.msToAr(900), 1E-15);
        assertEquals(10, OsuApiBeatmap.msToAr(450), 1E-15);
        assertEquals(11, OsuApiBeatmap.msToAr(300), 1E-15);
    }

    @Test
    public void testOdToMs() throws Exception {
        assertEquals(79.5, OsuApiBeatmap.odToMs(0), 1E-15);
        assertEquals(52.5, OsuApiBeatmap.odToMs(4.34), 1E-15);
        assertEquals(25.5, OsuApiBeatmap.odToMs(9), 1E-15);
        assertEquals(24.5, OsuApiBeatmap.odToMs(9.1), 1E-15);
        assertEquals(23.5, OsuApiBeatmap.odToMs(9.2), 1E-15);
        assertEquals(23.5, OsuApiBeatmap.odToMs(9.3), 1E-15);
        assertEquals(20.5, OsuApiBeatmap.odToMs(9.8), 1E-15);
    }

    @Test
    public void testCalcAR() throws Exception {
        assertEquals(-5, OsuApiBeatmap.calcAR(0, getMask(HalfTime, Easy)), 1E-15);
        assertEquals(-1, OsuApiBeatmap.calcAR(6, getMask(HalfTime, Easy)), 1E-15);
        assertEquals(5, OsuApiBeatmap.calcAR(7, getMask(HalfTime)), 1E-15);
        assertEquals(9, OsuApiBeatmap.calcAR(10, getMask(HalfTime, HardRock)), 1E-15);
        assertEquals(6 + 2 / 30d, OsuApiBeatmap.calcAR(4, getMask(DoubleTime, Easy)), 1E-15);
        assertEquals(9 + 2 / 3d, OsuApiBeatmap.calcAR(8, getMask(DoubleTime)), 2E-15);
        assertEquals(10 + 1 / 3d, OsuApiBeatmap.calcAR(9, getMask(DoubleTime)), 2E-15);
        assertEquals(11, OsuApiBeatmap.calcAR(10, getMask(DoubleTime, HardRock)), 0);
    }

    @Test
    public void testArToMs() {
        assertEquals(2400, OsuApiBeatmap.arToMs(-5), 1E-15);
        assertEquals(1800, OsuApiBeatmap.arToMs(0), 1E-15);
        assertEquals(1560, OsuApiBeatmap.arToMs(2), 1E-15);
        assertEquals(1200, OsuApiBeatmap.arToMs(5), 1E-15);
        assertEquals(900, OsuApiBeatmap.arToMs(7), 1E-15);
        assertEquals(450, OsuApiBeatmap.arToMs(10), 1E-15);
        assertEquals(300, OsuApiBeatmap.arToMs(11), 1E-15);
    }

    @Test
    public void testMsToOd() throws Exception {
        assertEquals(0, OsuApiBeatmap.msToOd(79.5), 1E-15);
        assertEquals(5, OsuApiBeatmap.msToOd(49.5), 1E-15);
        assertEquals(10, OsuApiBeatmap.msToOd(19.5), 1E-15);
        /*
         * this just kind of validates the formula, since non-integral od is not
         * bijective this way.
         */
    }

    @Test
    public void testOdToMsDT() throws Exception {
        assertEquals(13.67, OsuApiBeatmap.odToMs(9.8) * 2 / 3, 1E-2);
    }

    @Test
    public void testODBestFriendsHRDT() throws Exception {
        assertEquals(10.9722222222222, OsuApiBeatmap.calcOd(7, Mods.getMask(Mods.DoubleTime, Mods.HardRock)), 1E-10);
    }
}
