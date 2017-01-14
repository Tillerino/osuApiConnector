package org.tillerino.osuApiModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.types.BitwiseMods;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
    
	private Mods(@BitwiseMods long bit, String shortName, boolean effective) {
		this.bit = bit;
		this.shortName = shortName;
		this.effective = effective;
	}

	@BitwiseMods
	final long bit;
	String shortName;
	boolean effective;
	
	public String getShortName() {
		return shortName;
	}
	
	public boolean is(@BitwiseMods long mods) {
		return (mods & bit) == bit;
	}
	
	public static LinkedList<Mods> getMods(@BitwiseMods long mods) {
		LinkedList<Mods> ret = new LinkedList<>();
		
		Mods[] values = values();
		
		for (int i = 0; i < values.length; i++) {
			if(values[i].is(mods))
				ret.add(values[i]);
		}
		
		return ret;
	}
	
	public static LinkedList<Mods> fromShortNamesCommaSeparated(String modsString) {
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
	
	@CheckForNull
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
	
	public static @BitwiseMods long getMask(Mods ... mods) {
		return getMask(Arrays.asList(mods));
	}
	
	@SuppressFBWarnings(value="TQ", justification="producer")
	public static @BitwiseMods long add(@BitwiseMods long mods, Mods modToAdd) {
		return mods | modToAdd.bit;
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
	
	@SuppressFBWarnings(value = "TQ", justification = "producer")
	public static @BitwiseMods long getMask(Collection<Mods> mods) {
		long ret = 0;
		for(Mods m : mods) {
			ret |= m.bit;
		}
		return ret;
	}
	
	public boolean isEffective() {
		return effective;
	}
	
	@CheckForNull
	@SuppressFBWarnings(value = "TQ", justification = "producer")
	public static @BitwiseMods Long fromShortNamesContinuous(@Nonnull String message) {
		long mods = 0;
		for(int i = 0; i < message.length(); i+=2) {
			try {
				Mods mod = fromShortName(message.substring(i, i + 2).toUpperCase());
				if (mod == null) {
					return null;
				}
				if(mod.isEffective()) {
					if(mod == Nightcore) {
						mods |= getMask(DoubleTime);
					} else {
						mods |= getMask(mod);
					}
				}
			} catch(Exception e) {
				return null;
			}
		}
		return mods;
	}

	public static String toShortNamesContinuous(Collection<Mods> mods) {
		StringBuilder ret = new StringBuilder();
		
		for (Mods mod : mods) {
			ret.append(mod.getShortName());
		}
		
		return ret.toString();
	}
	
	@SuppressFBWarnings(value = "TQ", justification = "producer")
	public static @BitwiseMods long fixNC(@BitwiseMods long mods) {
		if((mods & Nightcore.bit) != 0) {
			mods |= DoubleTime.bit;
			mods &= ~Nightcore.bit;
		}
		return mods;
	}
}
