package org.tillerino.osuApiModel;

import static org.junit.Assert.*;

import org.junit.Test;


public class ModsTest {
	@Test
	public void testNightCoreFix() {
		assertEquals(Mods.DoubleTime.bit, Mods.fixNC(Mods.Nightcore.bit));
		assertEquals(Mods.DoubleTime.bit | Mods.Hidden.bit, Mods.fixNC(Mods.Nightcore.bit | Mods.Hidden.bit));
	}
}
