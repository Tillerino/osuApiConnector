package org.tillerino.osuApiModel;

import static org.tillerino.osuApiModel.Mods.*;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;


public class OsuApiBeatmapTest {
	@Test
	public void testRegression() throws IOException {
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
		expected.setStarDifficulty(2.40729);
		expected.setOverallDifficulty(6);
		expected.setCircleSize(4);
		expected.setApproachRate(6);
		expected.setHealthDrain(6);
		expected.setHitLength(109);
		expected.setTotalLength(142);
		expected.setMode(0);
		expected.setFileMd5("a5b99395a42bd55bc5eb1d2411cbdf8b");
		expected.setMaxCombo(314);
		expected.setAimDifficulty(1.19593);
		expected.setSpeedDifficulty(1.20622);
		expected.setTags("katamari");
		expected.setCreatorId(2);
		expected.setGenreId(2);
		expected.setLanguageId(3);

		OsuApiBeatmap downloaded = new Downloader().getBeatmap(75, OsuApiBeatmap.class);
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
		assertEquals(
				10.9722222222222,
				OsuApiBeatmap.calcOd(7,
						Mods.getMask(Mods.DoubleTime, Mods.HardRock)), 1E-10);
	}
}
