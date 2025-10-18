package org.tillerino.osuApiModel;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import lombok.Getter;
import org.tillerino.osuApiModel.types.BitwiseMods;

public enum Mods {
    /*
     * see https://github.com/peppy/osu-api/wiki
     * and https://osu.ppy.sh/wiki/en/Gameplay/Game_modifier
     */
    NoFail(1, "NF", true),
    Easy(2, "EZ", true),
    TouchDevice(4, null, true),
    Hidden(8, "HD", true),
    HardRock(16, "HR", true),
    SuddenDeath(32, "SD", false),
    DoubleTime(64, "DT", true),
    Relax(128, "RL", false),
    HalfTime(256, "HT", true),
    Nightcore(512, "NC", true),
    Flashlight(1024, "FL", true),
    Autoplay(2048, "AT", false),
    SpunOut(4096, "SO", true),
    /** Autopilot */
    Relax2(8192, "AP", false),
    Perfect(16384, "PF", false),
    Key4(32768, "4K", false),
    Key5(65536, "5K", false),
    Key6(131072, "6K", false),
    Key7(262144, "7K", false),
    Key8(524288, "8K", false),
    FadeIn(1048576, "FI", false),
    Random(2097152, "RD", false),
    Cinema(4194304, "CM", false),
    Target(8388608, "TP", false),
    Key9(16777216, "9K", false),
    KeyCoop(33554432, "CP", false),
    Key1(67108864, "1K", false),
    Key3(134217728, "3K", false),
    Key2(268435456, "2K", false),
    ScoreV2(536870912, "SV2", false),
    Mirror(1073741824, "MR", false),

    /**
     * This mod does not exist.
     * In the V2 API, the opposite - "Classic" exists.
     * Since this whole API is geared toward V1, we shoehorn compatibility in
     * by marking all non-classic V2 scores as "V2".
     * We use max positive value so that we don't interfere with future mods.
     */
    V2(1L << 62, null, false),
    ;

    static final HashMap<String, Mods> shortNames = new HashMap<>();

    Mods(@BitwiseMods long bit, String shortName, boolean effective) {
        this.bit = bit;
        this.shortName = shortName;
        this.effective = effective;
    }

    @BitwiseMods
    final long bit;

    @Getter
    final String shortName;

    @Getter
    final boolean effective;

    public boolean is(@BitwiseMods long mods) {
        return (mods & bit) == bit;
    }

    public static LinkedList<Mods> getMods(@BitwiseMods long mods) {
        LinkedList<Mods> ret = new LinkedList<>();

        Mods[] values = values();

        for (Mods value : values) {
            if (value.is(mods)) {
                ret.add(value);
            }
        }

        return ret;
    }

    public static LinkedList<Mods> fromShortNamesCommaSeparated(String modsString) {
        prepare();
        LinkedList<Mods> ret = new LinkedList<>();
        if (modsString.equals("None")) return ret;
        String[] modsStrings = modsString.split(",");
        for (String string : modsStrings) {
            if (string.isEmpty()) {
                continue;
            }

            Mods mod = shortNames.get(string);
            if (mod == null) {
                throw new RuntimeException(string);
            }

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
        if (shortNames.isEmpty()) {
            Mods[] values = values();
            for (Mods value : values) {
                if (value.shortName == null) {
                    continue;
                }
                shortNames.put(value.shortName, value);
            }
        }
    }

    public static @BitwiseMods long getMask(Mods... mods) {
        return getMask(Arrays.asList(mods));
    }

    @SuppressFBWarnings(value = "TQ", justification = "producer")
    public static @BitwiseMods long add(@BitwiseMods long mods, Mods modToAdd) {
        return mods | modToAdd.bit;
    }

    public static LinkedList<Mods> getEffectiveMods(List<Mods> mods) {
        LinkedList<Mods> ret = new LinkedList<>();

        for (Mods mod : mods) {
            if (mod.effective) {
                ret.add(mod);
            }
        }

        return ret;
    }

    public static LinkedList<Mods> getEffectiveMods() {
        LinkedList<Mods> ret = new LinkedList<>();

        Mods[] values = values();
        for (Mods mod : values) {
            if (mod.effective) ret.add(mod);
        }

        return ret;
    }

    @SuppressFBWarnings(value = "TQ", justification = "producer")
    public static @BitwiseMods long getMask(Collection<Mods> mods) {
        long ret = 0;
        for (Mods m : mods) {
            ret |= m.bit;
        }
        return ret;
    }

    @CheckForNull
    @SuppressFBWarnings(value = "TQ", justification = "producer")
    public static @BitwiseMods Long fromShortNamesContinuous(@Nonnull String message) {
        long mods = 0;
        for (int i = 0; i < message.length(); i += 2) {
            try {
                Mods mod = fromShortName(message.substring(i, i + 2).toUpperCase());
                if (mod == null) {
                    return null;
                }
                if (mod.isEffective()) {
                    if (mod == Nightcore) {
                        mods |= getMask(DoubleTime);
                    } else {
                        mods |= getMask(mod);
                    }
                }
            } catch (Exception e) {
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
        if ((mods & Nightcore.bit) != 0) {
            mods |= DoubleTime.bit;
            mods &= ~Nightcore.bit;
        }
        return mods;
    }
}
