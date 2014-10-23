package org.tillerino.osuApiModel;

import java.io.IOException;
import java.util.List;

import org.junit.Test;


public class OsuApiScoreTest {
	@Test
	public void testDownloadUserTop50() throws IOException {
		List<OsuApiScore> scores = new Downloader().getUserTop(2, GameModes.OSU, 50, OsuApiScore.class);
	}
}
