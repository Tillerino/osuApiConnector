package org.tillerino.osuApiModel.v2.utils;

import org.mapstruct.factory.Mappers;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.v2.OsuApiBeatmapV2;
import org.tillerino.osuApiModel.v2.mappers.BeatmapMapper;

public class BeatmapConversionUtil {
    private static final BeatmapMapper MAPPER = Mappers.getMapper(BeatmapMapper.class);

    public static OsuApiBeatmap fromV2Beatmap(OsuApiBeatmapV2 node) {
        return MAPPER.mapToV1(node);
    }
}
