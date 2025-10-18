package org.tillerino.osuApiModel.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BeatmapSetId;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.UserId;

record OsuApiBeatmapV2(
        @JsonProperty(required = true) @BeatmapId int id,
        @JsonProperty(required = true) @BeatmapSetId int beatmapset_id,
        @JsonProperty(required = true) @UserId int user_id,
        @JsonProperty(required = true) String version,
        @JsonProperty(required = true) String status,
        @JsonProperty(required = true) String last_updated,
        @JsonProperty(required = true) double bpm,
        @JsonProperty(required = true) double difficulty_rating,
        @JsonProperty(required = true) double accuracy,
        @JsonProperty(required = true) double ar,
        @JsonProperty(required = true) double cs,
        @JsonProperty(required = true) double drain,
        @JsonProperty(required = true) int hit_length,
        @JsonProperty(required = true) int total_length,
        @JsonProperty(required = true) @GameMode int mode_int,
        @JsonProperty(required = true) String checksum,
        @JsonProperty(required = true) int playcount,
        @JsonProperty(required = true) int passcount,
        @JsonProperty(required = true) int max_combo,
        @JsonProperty(required = true) Beatmapset beatmapset,
        @JsonProperty(required = true) Attributes attributes) {

    record Beatmapset(
            @JsonProperty(required = true) String title,
            @JsonProperty(required = true) String artist,
            @JsonProperty(required = true) String source,
            @JsonProperty(required = true) String creator,
            @JsonProperty(required = true) String tags,
            @JsonProperty(required = true) int genre_id,
            @JsonProperty(required = true) int language_id,
            @JsonProperty(required = true) String ranked_date,
            @JsonProperty(required = true) int favourite_count) {}

    record Attributes(
            @JsonProperty(required = true) double aim_difficulty,
            @JsonProperty(required = true) double speed_difficulty) {}
}
