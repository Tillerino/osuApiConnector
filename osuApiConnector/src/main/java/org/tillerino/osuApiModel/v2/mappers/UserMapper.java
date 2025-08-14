package org.tillerino.osuApiModel.v2.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.v2.OsuApiUserV2;

@Mapper
public interface UserMapper {
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
    OsuApiUser mapToV1(OsuApiUserV2 v2);
}
