package org.tillerino.osuApiModel.v2;

import java.util.List;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.UserId;

record OsuApiScoreBeatmapV2(
        @BeatmapId int beatmap_id,
        long score,
        int max_combo,
        boolean perfect,
        List<String> mods,
        @UserId int user_id,
        String created_at,
        String rank,
        Double pp,
        @GameMode int mode_int,
        Statistics statistics) {

    record Statistics(int count_300, int count_100, int count_50, int count_miss, int count_katu, int count_geki) {}
}
