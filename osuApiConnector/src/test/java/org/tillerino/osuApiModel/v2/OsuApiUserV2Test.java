package org.tillerino.osuApiModel.v2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Test;
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.OsuApiUser;

public class OsuApiUserV2Test {
    @Test
    public void testTillerino() throws IOException {
        OsuApiUser user = new DownloaderV2().getUser("Tillerino", GameModes.OSU);

        assertEquals(2070907, user.getUserId());

        System.out.println(user);
    }

    @Test
    public void testTillerinoById() throws IOException {
        OsuApiUser user = new DownloaderV2().getUser(2070907, GameModes.OSU);

        assertEquals("Tillerino", user.getUserName());

        System.out.println(user);
    }
}
