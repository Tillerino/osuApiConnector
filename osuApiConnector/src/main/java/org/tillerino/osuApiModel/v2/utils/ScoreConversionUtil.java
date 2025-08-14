package org.tillerino.osuApiModel.v2.utils;

import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.factory.Mappers;
import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.v2.OsuApiScoreBeatmapV2;
import org.tillerino.osuApiModel.v2.OsuApiScoreV2;
import org.tillerino.osuApiModel.v2.mappers.ScoreMapper;

public class ScoreConversionUtil {
    private static final ScoreMapper MAPPER = Mappers.getMapper(ScoreMapper.class);

    public static OsuApiScore fromV2Score(OsuApiScoreV2 node) {
        return MAPPER.mapToV1(node);
    }

    public static List<OsuApiScore> fromV2Score(List<OsuApiScoreV2> nodeList) {
        return nodeList.stream().map(MAPPER::mapToV1).collect(Collectors.toList());
    }

    public static OsuApiScore fromV2ScoreBeatmap(OsuApiScoreBeatmapV2 node) {
        return MAPPER.mapBeatmapScoreToV1(node);
    }

    public static List<OsuApiScore> fromV2ScoreBeatmap(List<OsuApiScoreBeatmapV2> nodeList) {
        return nodeList.stream().map(MAPPER::mapBeatmapScoreToV1).collect(Collectors.toList());
    }
}
