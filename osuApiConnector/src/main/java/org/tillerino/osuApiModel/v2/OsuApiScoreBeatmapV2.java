package org.tillerino.osuApiModel.v2;

import java.util.List;

record OsuApiScoreBeatmapV2(
        int beatmap_id,
        long score,
        int max_combo,
        boolean perfect,
        List<String> mods,
        int user_id,
        String created_at,
        String rank,
        Double pp,
        int mode_int,
        Statistics statistics) {

    record Statistics(int count_300, int count_100, int count_50, int count_miss, int count_katu, int count_geki) {}
}
