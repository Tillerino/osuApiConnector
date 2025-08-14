package org.tillerino.osuApiModel.v2.mappers;

import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.v2.OsuApiBeatmapV2;

@Mapper
public interface BeatmapMapper {
    @Mapping(source = "id", target = "beatmapId")
    @Mapping(source = "beatmapset_id", target = "setId")
    @Mapping(source = "beatmapset.title", target = "title")
    @Mapping(source = "beatmapset.artist", target = "artist")
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
    OsuApiBeatmap mapToV1(OsuApiBeatmapV2 beatmapV2);

    @Named("statusToApproved")
    static int convertStatusToApproved(String status) {
        switch (status) {
            case "ranked":
                return OsuApiBeatmap.RANKED;
            case "approved":
                return OsuApiBeatmap.APPROVED;
            case "qualified":
                return OsuApiBeatmap.QUALIFIED;
            case "loved":
                return OsuApiBeatmap.LOVED;
            case "pending":
                return OsuApiBeatmap.PENDING;
            case "wip":
                return OsuApiBeatmap.WIP;
            case "graveyard":
                return OsuApiBeatmap.GRAVEYARD;
            default:
                return -3;
        }
    }

    @Named("parseDate")
    static long parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return 0;
        }
        try {
            Instant instant = Instant.parse(dateString);
            return instant.toEpochMilli();
        } catch (Exception e) {
            return 0;
        }
    }
}
