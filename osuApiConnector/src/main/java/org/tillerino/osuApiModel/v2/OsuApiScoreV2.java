package org.tillerino.osuApiModel.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.UserId;

record OsuApiScoreV2(
        @JsonProperty(required = true, value = "total_score") long score,
        @JsonProperty(required = true, value = "legacy_total_score") long legacyScore,
        @JsonProperty(required = true, value = "max_combo") int maxCombo,
        @JsonProperty(required = true, value = "is_perfect_combo") boolean perfect,
        @JsonProperty(required = true) List<ModWrapper> mods,
        @JsonProperty(required = true, value = "user_id") @UserId int userId,
        @JsonProperty(required = true, value = "ended_at") String date,
        @JsonProperty(required = true) String rank,
        @JsonProperty(required = true) Double pp,
        @JsonProperty(required = true) Beatmap beatmap,
        @JsonProperty(required = true) ScoreStatistics statistics) {

    record Beatmap(@JsonProperty(required = true) @BeatmapId int id) {}

    record ScoreStatistics(int great, int ok, int meh, int miss, int large_bonus, int small_bonus) {}

    record ModWrapper(@JsonProperty(required = true) String acronym) {}
}
