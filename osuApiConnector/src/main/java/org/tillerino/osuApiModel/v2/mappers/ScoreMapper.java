package org.tillerino.osuApiModel.v2.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.deserializer.BitwiseToMods;
import org.tillerino.osuApiModel.v2.OsuApiScoreBeatmapV2;
import org.tillerino.osuApiModel.v2.OsuApiScoreV2;

@Mapper
public interface ScoreMapper {
    @Mapping(source = "beatmap.id", target = "beatmapId")
    @Mapping(source = "max_combo", target = "maxCombo")
    @Mapping(source = "statistics.count_300", target = "count300")
    @Mapping(source = "statistics.count_100", target = "count100")
    @Mapping(source = "statistics.count_50", target = "count50")
    @Mapping(source = "statistics.count_miss", target = "countMiss")
    @Mapping(source = "statistics.count_katu", target = "countKatu")
    @Mapping(source = "statistics.count_geki", target = "countGeki")
    @Mapping(target = "perfect", expression = "java(scoreV2.isPerfect() ? 1 : 0)")
    @Mapping(source = "mods", target = "mods", qualifiedByName = "modsToBitwise")
    @Mapping(source = "user_id", target = "userId")
    @Mapping(source = "created_at", target = "date", qualifiedByName = "isoToEpoch")
    @Mapping(source = "rank", target = "rank")
    @Mapping(source = "pp", target = "pp")
    @Mapping(source = "mode_int", target = "mode")
    OsuApiScore mapToV1(OsuApiScoreV2 scoreV2);

    @Mapping(source = "beatmap_id", target = "beatmapId")
    @Mapping(source = "max_combo", target = "maxCombo")
    @Mapping(source = "statistics.count_300", target = "count300")
    @Mapping(source = "statistics.count_100", target = "count100")
    @Mapping(source = "statistics.count_50", target = "count50")
    @Mapping(source = "statistics.count_miss", target = "countMiss")
    @Mapping(source = "statistics.count_katu", target = "countKatu")
    @Mapping(source = "statistics.count_geki", target = "countGeki")
    @Mapping(target = "perfect", expression = "java(scoreV2.isPerfect() ? 1 : 0)")
    @Mapping(source = "mods", target = "mods", qualifiedByName = "modsToBitwise")
    @Mapping(source = "user_id", target = "userId")
    @Mapping(source = "created_at", target = "date", qualifiedByName = "isoToEpoch")
    @Mapping(source = "rank", target = "rank")
    @Mapping(source = "pp", target = "pp")
    @Mapping(source = "mode_int", target = "mode")
    OsuApiScore mapBeatmapScoreToV1(OsuApiScoreBeatmapV2 scoreV2);

    @Named("modsToBitwise")
    public static long modsToBitwise(JsonNode modsArray) {
        return BitwiseToMods.modsArrayToBitwise(modsArray);
    }

    @Named("isoToEpoch")
    public static long isoToEpoch(String isoDate) {
        return isoDate == null ? 0L : Instant.parse(isoDate).toEpochMilli();
    }
}
