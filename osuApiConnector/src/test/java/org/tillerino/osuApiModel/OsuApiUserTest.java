package org.tillerino.osuApiModel;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class OsuApiUserTest {
	@Test
	public void testTillerino() throws IOException {
		OsuApiUser user = new Downloader().getUser("Tillerino", GameMode.OSU, OsuApiUser.class);
		
		assertEquals(2070907, user.getUserId());
		
		System.out.println(user);
	}
	
	@Test
	public void testTillerinoById() throws IOException {
		OsuApiUser user = new Downloader().getUser(2070907, GameMode.OSU, OsuApiUser.class);
		
		assertEquals("Tillerino", user.getUsername());
		
		System.out.println(user);
	}
}
