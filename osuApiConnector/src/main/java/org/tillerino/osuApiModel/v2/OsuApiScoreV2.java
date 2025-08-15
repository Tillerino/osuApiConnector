package org.tillerino.osuApiModel.v2;

import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.UserId;

import java.util.List;

record OsuApiScoreV2(
        long score,
        int max_combo,
        boolean perfect,
        List<String> mods,
        @UserId
        int user_id,
        String created_at,
        String rank,
        Double pp,
        @GameMode
        int mode_int,
        Beatmap beatmap,
        Statistics statistics) {

    record Beatmap(@BeatmapId int id) {}

    record Statistics(int count_300, int count_100, int count_50, int count_miss, int count_katu, int count_geki) {}
}
