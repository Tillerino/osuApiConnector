package org.tillerino.osuApiModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public enum Mods {
	/*
	 * see https://github.com/peppy/osu-api/wiki
	 */
    NoFail(1, "NF", true),
    Easy(2, "EZ", true),
    NoVideo(4, null, false),
    Hidden(8, "HD", true),
    HardRock(16, "HR", true),
    SuddenDeath(32, "SD", false),
    DoubleTime(64, "DT", true),
    Relax(128, null, false),
    HalfTime(256, "HT", true),
    Nightcore(512, "NC", true),
    Flashlight(1024, "FL", true),
    Autoplay(2048, null, false),
    SpunOut(4096, "SO", true),
    Relax2(8192, null, false),
    Perfect(16384, "PF", false),
    Key4(32768, null, false),
    Key5(65536, null, false),
    Key6(131072, null, false),
    Key7(262144, null, false),
    Key8(524288, null, false),
    FadeIn(1048576, null, false),
    Random(2097152, null, false),
    LastMod(4194304, null, false),
    ;
	
    static HashMap<String, Mods> shortNames = new HashMap<>();
    
	private Mods(long bit, String shortName, boolean effective) {
		this.bit = bit;
		this.shortName = shortName;
		this.effective = effective;
	}

	final long bit;
	String shortName;
	boolean effective;
	
	public String getShortName() {
		return shortName;
	}
	
	public boolean is(long mods) {
		return (mods & bit) == bit;
	}
	
	public static LinkedList<Mods> getMods(long mods) {
		LinkedList<Mods> ret = new LinkedList<>();
		
		Mods[] values = values();
		
		for (int i = 0; i < values.length; i++) {
			if(values[i].is(mods))
				ret.add(values[i]);
		}
		
		return ret;
	}
	
	public static LinkedList<Mods> getMods(String modsString) {
		prepare();
		LinkedList<Mods> ret = new LinkedList<>();
		if(modsString.equals("None"))
			return ret;
		String[] modsStrings = modsString.split(",");
		for (int i = 0; i < modsStrings.length; i++) {
			if(modsStrings[i].length() == 0)
				continue;
			
			Mods mod = shortNames.get(modsStrings[i]);
			if(mod == null)
				throw new RuntimeException(modsStrings[i]);
			
			ret.add(mod);
		}
		return ret;
	}
	
	public static Mods fromShortName(String shortName) {
		prepare();
		return shortNames.get(shortName);
	}

	private static void prepare() {
		if(shortNames.isEmpty()) {
			Mods[] values = values();
			for (int i = 0; i < values.length; i++) {
				if(values[i].shortName == null)
					continue;
				shortNames.put(values[i].shortName, values[i]);
			}
		}
	}
	
	public static long getMask(Mods ... mods) {
		long ret = 0l;
		
		for (int i = 0; i < mods.length; i++) {
			ret |= mods[i].bit;
		}
		
		return ret;
	}
	
	public static LinkedList<Mods> getEffectiveMods(List<Mods> mods) {
		LinkedList<Mods> ret = new LinkedList<>();
		
		for (Mods mod : mods) {
			if(mod.effective)
				ret.add(mod);
		}
		
		return ret;
	}
	
	public static LinkedList<Mods> getEffectiveMods() {
		LinkedList<Mods> ret = new LinkedList<>();
		
		Mods[] values = values();
		for (int i = 0; i < values.length; i++) {
			Mods mod = values[i];
			if(mod.effective)
				ret.add(mod);
		}
		
		return ret;
	}
	
	public static long getMask(Collection<Mods> mods) {
		long ret = 0;
		for(Mods m : mods) {
			ret |= m.bit;
		}
		return ret;
	}
	
	public boolean isEffective() {
		return effective;
	}
	
	public static String getShortNames(Collection<Mods> mods) {
		String ret = "";
		
		for (Mods mod : mods) {
			ret += mod.getShortName();
		}
		
		return ret;
	}
}
