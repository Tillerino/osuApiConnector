package org.tillerino.osuApiModel.v2;

import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.OsuApiScore;

public class OsuApiScoreV2Test {
    @Test
    public void testDownloadUserTop50() throws IOException {
        List<OsuApiScore> scores = new DownloaderV2().getUserTop(2, GameModes.OSU, 50);
    }
}
