package org.tillerino.osuApiModel;

import java.io.IOException;
import java.util.List;
import javax.annotation.CheckForNull;
import org.tillerino.osuApiModel.types.*;

public interface OsuApiClient {
    @CheckForNull
    <T extends OsuApiBeatmap> T getBeatmap(@BeatmapId int beatmapId, long mods, Class<T> cls) throws IOException;

    <T extends OsuApiScore> List<T> getUserTop(@UserId int userId, @GameMode int mode, int limit, Class<T> cls)
            throws IOException;

    @CheckForNull
    <T extends OsuApiUser> T getUser(@UserId int userId, @GameMode int mode, Class<T> cls) throws IOException;

    @CheckForNull
    <T extends OsuApiUser> T getUser(String username, @GameMode int mode, Class<T> cls) throws IOException;

    <T extends OsuApiScore> List<T> getUserRecent(@UserId int userid, @GameMode int mode, Class<T> cls)
            throws IOException;
}
