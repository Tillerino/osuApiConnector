package org.tillerino.osuApiModel.v2.utils;

import org.mapstruct.factory.Mappers;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.v2.OsuApiUserV2;
import org.tillerino.osuApiModel.v2.mappers.UserMapper;

public class UserConversionUtil {
    private static final UserMapper MAPPER = Mappers.getMapper(UserMapper.class);

    public static OsuApiUser fromV2User(OsuApiUserV2 node) {
        return MAPPER.mapToV1(node);
    }
}
