package org.tillerino.osuApiModel.v2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Test;
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.OsuApiUser;

public class OsuApiUserV2Test {
    @Test
    public void testTillerino() throws IOException {
        OsuApiUser user = DownloaderV2Test.getProdDownloader().getUser("Tillerino", GameModes.OSU, OsuApiUser.class);

        assertEquals(2070907, user.getUserId());

        System.out.println(user);
    }

    @Test
    public void testTillerinoById() throws IOException {
        OsuApiUser user = DownloaderV2Test.getProdDownloader().getUser(2070907, GameModes.OSU, OsuApiUser.class);

        assertEquals("Tillerino", user.getUserName());

        System.out.println(user);
    }
}
