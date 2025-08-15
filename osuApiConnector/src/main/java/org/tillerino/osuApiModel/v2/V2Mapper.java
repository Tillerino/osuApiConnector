package org.tillerino.osuApiModel.v2;

import java.time.Instant;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
interface V2Mapper {
    @Mapping(source = "beatmap.id", target = "beatmapId")
    @Mapping(source = "max_combo", target = "maxCombo")
    @Mapping(source = "statistics.count_300", target = "count300")
    @Mapping(source = "statistics.count_100", target = "count100")
    @Mapping(source = "statistics.count_50", target = "count50")
    @Mapping(source = "statistics.count_miss", target = "countMiss")
    @Mapping(source = "statistics.count_katu", target = "countKatu")
    @Mapping(source = "statistics.count_geki", target = "countGeki")
    @Mapping(target = "perfect", expression = "java(scoreV2.perfect() ? 1 : 0)")
    @Mapping(source = "mods", target = "mods", qualifiedByName = "modsToBitwise")
    @Mapping(source = "user_id", target = "userId")
    @Mapping(source = "created_at", target = "date", qualifiedByName = "isoToEpoch")
    @Mapping(source = "rank", target = "rank")
    @Mapping(source = "pp", target = "pp")
    @Mapping(source = "mode_int", target = "mode")
    @Mapping(target = "modsList", ignore = true)
    OsuApiScore mapScoreToV1(OsuApiScoreV2 scoreV2);

    @Mapping(source = "beatmap_id", target = "beatmapId")
    @Mapping(source = "max_combo", target = "maxCombo")
    @Mapping(source = "statistics.count_300", target = "count300")
    @Mapping(source = "statistics.count_100", target = "count100")
    @Mapping(source = "statistics.count_50", target = "count50")
    @Mapping(source = "statistics.count_miss", target = "countMiss")
    @Mapping(source = "statistics.count_katu", target = "countKatu")
    @Mapping(source = "statistics.count_geki", target = "countGeki")
    @Mapping(target = "perfect", expression = "java(scoreV2.perfect() ? 1 : 0)")
    @Mapping(source = "mods", target = "mods", qualifiedByName = "modsToBitwise")
    @Mapping(source = "user_id", target = "userId")
    @Mapping(source = "created_at", target = "date", qualifiedByName = "isoToEpoch")
    @Mapping(source = "rank", target = "rank")
    @Mapping(source = "pp", target = "pp")
    @Mapping(source = "mode_int", target = "mode")
    @Mapping(target = "modsList", ignore = true)
    OsuApiScore mapBeatmapScoreToV1(OsuApiScoreBeatmapV2 scoreV2);

    @Named("modsToBitwise")
    @BitwiseMods
    static long modsToBitwise(List<String> modsArray) {
        if (modsArray == null) {
            return 0L;
        }
        return Mods.getMask(modsArray.stream().map(Mods::fromShortName).toList());
    }

    @Named("isoToEpoch")
    @MillisSinceEpoch
    @SuppressFBWarnings("TQ_UNKNOWN_VALUE_USED_WHERE_ALWAYS_STRICTLY_REQUIRED")
    static long isoToEpoch(String isoDate) {
        return isoDate == null ? 0L : Instant.parse(isoDate).toEpochMilli();
    }

    @Mapping(source = "id", target = "beatmapId")
    @Mapping(source = "beatmapset_id", target = "setId")
    @Mapping(source = "beatmapset.title", target = "title")
    @Mapping(source = "beatmapset.artist", target = "artist")
    @Mapping(source = "beatmapset.source", target = "source")
    @Mapping(source = "beatmapset.creator", target = "creator")
    @Mapping(source = "beatmapset.tags", target = "tags")
    @Mapping(source = "user_id", target = "creatorId")
    @Mapping(source = "beatmapset.genre_id", target = "genreId")
    @Mapping(source = "beatmapset.language_id", target = "languageId")
    @Mapping(source = "status", target = "approved", qualifiedByName = "statusToApproved")
    @Mapping(source = "beatmapset.ranked_date", target = "approvedDate", qualifiedByName = "parseDate")
    @Mapping(source = "last_updated", target = "lastUpdate", qualifiedByName = "parseDate")
    @Mapping(source = "difficulty_rating", target = "starDifficulty")
    @Mapping(source = "attributes.aim_difficulty", target = "aimDifficulty")
    @Mapping(source = "attributes.speed_difficulty", target = "speedDifficulty")
    @Mapping(source = "accuracy", target = "overallDifficulty")
    @Mapping(source = "cs", target = "circleSize")
    @Mapping(source = "ar", target = "approachRate")
    @Mapping(source = "drain", target = "healthDrain")
    @Mapping(source = "hit_length", target = "hitLength")
    @Mapping(source = "total_length", target = "totalLength")
    @Mapping(source = "mode_int", target = "mode")
    @Mapping(source = "checksum", target = "fileMd5")
    @Mapping(source = "beatmapset.favourite_count", target = "favouriteCount")
    @Mapping(source = "playcount", target = "playCount")
    @Mapping(source = "passcount", target = "passCount")
    @Mapping(source = "max_combo", target = "maxCombo")
    OsuApiBeatmap mapBeatmapToV1(OsuApiBeatmapV2 beatmapV2);

    @Named("statusToApproved")
    static int convertStatusToApproved(String status) {
        return switch (status) {
            case "ranked" -> OsuApiBeatmap.RANKED;
            case "approved" -> OsuApiBeatmap.APPROVED;
            case "qualified" -> OsuApiBeatmap.QUALIFIED;
            case "loved" -> OsuApiBeatmap.LOVED;
            case "pending" -> OsuApiBeatmap.PENDING;
            case "wip" -> OsuApiBeatmap.WIP;
            case "graveyard" -> OsuApiBeatmap.GRAVEYARD;
            default -> -3;
        };
    }

    @Named("parseDate")
    @MillisSinceEpoch
    @SuppressFBWarnings("TQ_UNKNOWN_VALUE_USED_WHERE_ALWAYS_STRICTLY_REQUIRED")
    static long parseDate(String dateString) {
        try {
            Instant instant = Instant.parse(dateString);
            return instant.toEpochMilli();
        } catch (Exception e) {
            return 0;
        }
    }

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "country.code", target = "country")
    @Mapping(source = "statistics.count_300", target = "count300")
    @Mapping(source = "statistics.count_100", target = "count100")
    @Mapping(source = "statistics.count_50", target = "count50")
    @Mapping(source = "statistics.play_count", target = "playCount")
    @Mapping(source = "statistics.ranked_score", target = "rankedScore")
    @Mapping(source = "statistics.total_score", target = "totalScore")
    @Mapping(source = "statistics.global_rank", target = "rank")
    @Mapping(source = "statistics.level.current", target = "level")
    @Mapping(source = "statistics.pp", target = "pp")
    @Mapping(source = "statistics.hit_accuracy", target = "accuracy")
    @Mapping(source = "statistics.grade_counts.ss", target = "countSS")
    @Mapping(source = "statistics.grade_counts.s", target = "countS")
    @Mapping(source = "statistics.grade_counts.a", target = "countA")
    @Mapping(source = "modeInt", target = "mode")
    @Mapping(source = "username", target = "userName")
    OsuApiUser mapUserToV1(OsuApiUserV2 v2);
}
