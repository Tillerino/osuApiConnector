package org.tillerino.osuApiModel.v2;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.mapstruct.*;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;
import org.tillerino.osuApiModel.v2.OsuApiScoreV2.ModWrapper;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
interface V2Mapper {
    @Mapping(source = "beatmap.id", target = "beatmapId")
    @Mapping(source = "statistics.great", target = "count300")
    @Mapping(source = "statistics.ok", target = "count100")
    @Mapping(source = "statistics.meh", target = "count50")
    @Mapping(source = "statistics.miss", target = "countMiss")
    @Mapping(source = "statistics.small_bonus", target = "countKatu")
    @Mapping(source = "statistics.large_bonus", target = "countGeki")
    @Mapping(target = "perfect", expression = "java(scoreV2.perfect() ? 1 : 0)")
    @Mapping(source = "mods", target = "mods", qualifiedByName = "modsToBitwise")
    @Mapping(target = "date", qualifiedByName = "isoToEpoch")
    @Mapping(target = "mode", ignore = true)
    @Mapping(target = "modsList", ignore = true)
    void mapScoreToV1(OsuApiScoreV2 scoreV2, @MappingTarget OsuApiScore target);

    default <T extends OsuApiScore> T mapScoreToV1(OsuApiScoreV2 scoreV2, Class<T> cls, @GameMode int mode) {
        T target = newInstance(cls);
        mapScoreToV1(scoreV2, target);
        target.setMode(mode);
        return target;
    }

    @Mapping(source = "statistics.great", target = "count300")
    @Mapping(source = "statistics.ok", target = "count100")
    @Mapping(source = "statistics.meh", target = "count50")
    @Mapping(source = "statistics.miss", target = "countMiss")
    @Mapping(source = "statistics.small_bonus", target = "countKatu")
    @Mapping(source = "statistics.large_bonus", target = "countGeki")
    @Mapping(target = "perfect", expression = "java(scoreV2.perfect() ? 1 : 0)")
    @Mapping(target = "mods", qualifiedByName = "modsToBitwise")
    @Mapping(target = "date", qualifiedByName = "isoToEpoch")
    @Mapping(target = "mode", ignore = true)
    @Mapping(target = "modsList", ignore = true)
    void mapBeatmapScoreToV1(OsuApiScoreBeatmapV2 scoreV2, @MappingTarget OsuApiScore target);

    default <T extends OsuApiScore> T mapBeatmapScoreToV1(OsuApiScoreBeatmapV2 scoreV2, Class<T> cls, @GameMode int mode) {
        T target = newInstance(cls);
        mapBeatmapScoreToV1(scoreV2, target);
        target.setMode(mode);
        return target;
    }

    @Named("modsToBitwise")
    @BitwiseMods
    @SuppressFBWarnings("TQ_UNKNOWN_VALUE_USED_WHERE_ALWAYS_STRICTLY_REQUIRED")
    static long modsToBitwise(List<ModWrapper> modsArray) {
        if (modsArray == null) {
            return 0L;
        }
        List<String> extracted =
                modsArray.stream().map(ModWrapper::acronym).collect(Collectors.toCollection(ArrayList::new));
        boolean classic = extracted.remove("CL");
        long mask = Mods.getMask(extracted.stream()
                .map(Mods::fromShortName)
                .filter(Objects::nonNull)
                .toList());
        if (!classic) {
            mask |= Mods.getMask(Mods.V2);
        }
        return mask;
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
    void mapBeatmapToV1(OsuApiBeatmapV2 beatmapV2, @MappingTarget OsuApiBeatmap target);

    default <T extends OsuApiBeatmap> T mapBeatmapToV1(OsuApiBeatmapV2 beatmapNode, Class<T> cls) {
        T target = newInstance(cls);
        mapBeatmapToV1(beatmapNode, target);
        return target;
    }

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
    @Mapping(target = "mode", ignore = true)
    @Mapping(source = "username", target = "userName")
    void mapUserToV1(OsuApiUserV2 v2, @MappingTarget OsuApiUser target);

    default <T extends OsuApiUser> T mapUserToV1(OsuApiUserV2 userV2, Class<T> cls, @GameMode int mode) {
        T target = newInstance(cls);
        mapUserToV1(userV2, target);
        target.setMode(mode);
        return target;
    }

    private static <T> T newInstance(Class<T> cls) {
        try {
            return cls.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
