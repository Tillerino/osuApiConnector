package org.tillerino.osuApiModel;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;


public class OsuApiBeatmapTest {
	@Test
	public void testRegression() throws IOException {
		OsuApiBeatmap expected = new OsuApiBeatmap();
		
		expected.id=75; expected.setId=1; expected.artist="Kenji Ninuma"; expected.title="DISCO PRINCE"; expected.version="Normal"; expected.creator="peppy"; expected.source=""; expected.approved=1; expected.approvedDate=1191692791000l; expected.lastUpdate=1191692791000l; expected.bpm=119.999; expected.starDifficulty=2.2918; expected.overallDifficulty=6; expected.circleSize=4; expected.approachRate=6; expected.healthDrain=6; expected.hitLength=108; expected.totalLength=141; expected.mode=0;
		
		OsuApiBeatmap downloaded = new Downloader().getBeatmap(75, OsuApiBeatmap.class);
		
		assertEquals(expected, downloaded);
	}
}
