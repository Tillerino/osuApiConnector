package org.tillerino.osuApiModel.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.osuApiModel.v2.OsuApiScoreV2.ModWrapper;
import org.tillerino.osuApiModel.v2.OsuApiScoreV2.ScoreStatistics;

record OsuApiScoreBeatmapV2(
        @JsonProperty(required = true, value = "beatmap_id") @BeatmapId int beatmapId,
        @JsonProperty(required = true, value = "total_score") long score,
        @JsonProperty(required = true, value = "legacy_total_score") long legacyScore,
        @JsonProperty(required = true, value = "max_combo") int maxCombo,
        @JsonProperty(required = true, value = "is_perfect_combo") boolean perfect,
        @JsonProperty(required = true) List<ModWrapper> mods,
        @JsonProperty(required = true, value = "user_id") @UserId int userId,
        @JsonProperty(required = true, value = "ended_at") String date,
        @JsonProperty(required = true) String rank,
        @JsonProperty(required = true) Double pp,
        @JsonProperty(required = true) ScoreStatistics statistics) {}
