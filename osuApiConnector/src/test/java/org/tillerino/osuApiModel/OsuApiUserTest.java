package org.tillerino.osuApiModel;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class OsuApiUserTest {
	@Test
	public void testTillerino() throws IOException {
		OsuApiUser user = new Downloader().getUser("Tillerino", GameModes.OSU, OsuApiUser.class);
		
		assertEquals(2070907, user.getUserId());
		
		System.out.println(user);
	}
	
	@Test
	public void testTillerinoById() throws IOException {
		OsuApiUser user = new Downloader().getUser(2070907, GameModes.OSU, OsuApiUser.class);
		
		assertEquals("Tillerino", user.getUserName());
		
		System.out.println(user);
	}
}
