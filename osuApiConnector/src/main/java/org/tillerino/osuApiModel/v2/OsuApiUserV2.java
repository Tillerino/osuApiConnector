package org.tillerino.osuApiModel.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.tillerino.osuApiModel.types.OsuName;
import org.tillerino.osuApiModel.types.UserId;

record OsuApiUserV2(
        @JsonProperty(required = true) @UserId int id,
        @JsonProperty(required = true) @OsuName String username,
        @JsonProperty(required = true) Country country,
        @JsonProperty(required = true) Statistics statistics) {

    record Country(@JsonProperty(required = true) String code) {}

    record Statistics(
            @JsonProperty(required = true) int count_300,
            @JsonProperty(required = true) int count_100,
            @JsonProperty(required = true) int count_50,
            @JsonProperty(required = true) int play_count,
            @JsonProperty(required = true) long ranked_score,
            @JsonProperty(required = true) long total_score,
            @JsonProperty(required = true) int global_rank,
            @JsonProperty(required = true) double pp,
            @JsonProperty(required = true) double hit_accuracy,
            @JsonProperty(required = true) Level level,
            @JsonProperty(required = true) GradeCounts grade_counts) {

        record Level(@JsonProperty(required = true) double current) {}

        record GradeCounts(
                @JsonProperty(required = true) int ss,
                @JsonProperty(required = true) int s,
                @JsonProperty(required = true) int a) {}
    }
}
