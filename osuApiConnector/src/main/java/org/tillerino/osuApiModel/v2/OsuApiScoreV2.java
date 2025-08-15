package org.tillerino.osuApiModel.v2;

import java.util.List;

record OsuApiScoreV2(
        long score,
        int max_combo,
        boolean perfect,
        List<String> mods,
        int user_id,
        String created_at,
        String rank,
        Double pp,
        int mode_int,
        Beatmap beatmap,
        Statistics statistics) {

    record Beatmap(int id) {}

    record Statistics(int count_300, int count_100, int count_50, int count_miss, int count_katu, int count_geki) {}
}
