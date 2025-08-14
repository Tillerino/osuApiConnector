package org.tillerino.osuApiModel;

import static org.junit.Assert.*;
import static org.tillerino.osuApiModel.Mods.*;

import com.google.common.net.MediaType;
import java.io.IOException;
import org.junit.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class OsuApiBeatmapTest extends AbstractMockServerTest {
    private OsuApiBeatmap expectedDiscoPrince() {
        OsuApiBeatmap expected = new OsuApiBeatmap();

        expected.setBeatmapId(75);
        expected.setSetId(1);
        expected.setArtist("Kenji Ninuma");
        expected.setTitle("DISCO PRINCE");
        expected.setVersion("Normal");
        expected.setCreator("peppy");
        expected.setSource("");
        expected.setApproved(1);
        expected.setApprovedDate(1191692791000L);
        expected.setLastUpdate(1191692791000L);
        expected.setBpm(119.999);
        expected.setStarDifficulty(2.55606);
        expected.setOverallDifficulty(6);
        expected.setCircleSize(4);
        expected.setApproachRate(6);
        expected.setHealthDrain(6);
        expected.setHitLength(109);
        expected.setTotalLength(142);
        expected.setMode(0);
        expected.setFileMd5("a5b99395a42bd55bc5eb1d2411cbdf8b");
        expected.setMaxCombo(314);
        expected.setAimDifficulty(1.28033);
        expected.setSpeedDifficulty(1.17561);
        expected.setTags("katamari");
        expected.setCreatorId(2);
        expected.setGenreId(2);
        expected.setLanguageId(3);
        return expected;
    }

    private static OsuApiBeatmap liveDiscoPrince() {
        OsuApiBeatmap expected = new OsuApiBeatmap();

        expected.setBeatmapId(75);
        expected.setSetId(1);
        expected.setArtist("Kenji Ninuma");
        expected.setTitle("DISCO PRINCE");
        expected.setVersion("Normal");
        expected.setCreator("peppy");
        expected.setSource("");
        expected.setApproved(1);
        expected.setApprovedDate(1191692791000L);
        expected.setLastUpdate(1191692791000L);
        expected.setBpm(120);
        expected.setStarDifficulty(2.57569);
        expected.setOverallDifficulty(6);
        expected.setCircleSize(4);
        expected.setApproachRate(6);
        expected.setHealthDrain(6);
        expected.setHitLength(109);
        expected.setTotalLength(142);
        expected.setMode(0);
        expected.setFileMd5("233f55099932d0696a3ef192041bc30d");
        expected.setMaxCombo(314);
        expected.setAimDifficulty(1.30555);
        expected.setSpeedDifficulty(1.13941);
        expected.setTags("katamari");
        expected.setCreatorId(2);
        expected.setGenreId(2);
        expected.setLanguageId(3);
        return expected;
    }

    @Test
    public void testRegressionDownload() throws IOException {
        OsuApiBeatmap expected = liveDiscoPrince();

        OsuApiBeatmap downloaded = new Downloader().getBeatmap(75, OsuApiBeatmap.class);
        assertNotNull(downloaded);

        expected.setPlayCount(downloaded.getPlayCount());
        expected.setPassCount(downloaded.getPassCount());
        expected.setFavouriteCount(downloaded.getFavouriteCount());

        assertEquals(expected, downloaded);
    }

    @Test
    public void testRegressionFixed() throws IOException {
        // https://osu.ppy.sh/api/get_beatmaps?k=...&b=75
        String s =
                "[{\"beatmapset_id\":\"1\",\"beatmap_id\":\"75\",\"approved\":\"1\",\"total_length\":\"142\",\"hit_length\":\"109\",\"version\":\"Normal\",\"file_md5\":\"a5b99395a42bd55bc5eb1d2411cbdf8b\",\"diff_size\":\"4\",\"diff_overall\":\"6\",\"diff_approach\":\"6\",\"diff_drain\":\"6\",\"mode\":\"0\",\"count_normal\":\"160\",\"count_slider\":\"30\",\"count_spinner\":\"4\",\"submit_date\":\"2007-10-06 17:46:31\",\"approved_date\":\"2007-10-06 17:46:31\",\"last_update\":\"2007-10-06 17:46:31\",\"artist\":\"Kenji Ninuma\",\"artist_unicode\":null,\"title\":\"DISCO PRINCE\",\"title_unicode\":null,\"creator\":\"peppy\",\"creator_id\":\"2\",\"bpm\":\"119.999\",\"source\":\"\",\"tags\":\"katamari\",\"genre_id\":\"2\",\"language_id\":\"3\",\"favourite_count\":\"1041\",\"rating\":\"8.24649\",\"storyboard\":\"0\",\"video\":\"0\",\"download_unavailable\":\"0\",\"audio_unavailable\":\"0\",\"playcount\":\"559067\",\"passcount\":\"67037\",\"packs\":\"S1,T23,T61\",\"max_combo\":\"314\",\"diff_aim\":\"1.28033\",\"diff_speed\":\"1.17561\",\"difficultyrating\":\"2.55606\"}]";
        mockServer
                .when(HttpRequest.request("/get_beatmaps")
                        .withQueryStringParameter("k", "key")
                        .withQueryStringParameter("b", "75"))
                .respond(HttpResponse.response().withBody(s, MediaType.JSON_UTF_8));
        OsuApiBeatmap expected = expectedDiscoPrince();

        OsuApiBeatmap downloaded = downloader.getBeatmap(75, OsuApiBeatmap.class);
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
