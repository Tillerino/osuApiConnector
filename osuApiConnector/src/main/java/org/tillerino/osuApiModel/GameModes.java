package org.tillerino.osuApiModel;

import org.tillerino.osuApiModel.types.GameMode;

public class GameModes {
    @GameMode
    public static final int OSU = 0;

    @GameMode
    public static final int TAIKO = 1;

    @GameMode
    public static final int CTB = 2;

    @GameMode
    public static final int MANIA = 3;

    private static final String[] modeRuleset = {"osu", "taiko", "fruits", "mania"};

    public static String getRulesetName(int mode) {
        if (mode < 0 || mode >= modeRuleset.length) {
            throw new IllegalArgumentException("Invalid mode " + mode);
        }

        return modeRuleset[mode];
    }
}
