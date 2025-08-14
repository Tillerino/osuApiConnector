package org.tillerino.osuApiModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.OsuName;
import org.tillerino.osuApiModel.types.UserId;

@Data
public class OsuApiUser {
    @JsonProperty("user_id")
    @UserId
    private int userId;

    @JsonProperty("username")
    @OsuName
    private String userName;
    /**
     * Total amount for all ranked and approved beatmaps played
     */
    private int count300;
    /**
     * Total amount for all ranked and approved beatmaps played
     */
    private int count100;
    /**
     * Total amount for all ranked and approved beatmaps played
     */
    private int count50;
    /**
     * Only counts ranked and approved beatmaps
     */
    @JsonProperty("playcount")
    private int playCount;
    /**
     * Counts the best individual score on each ranked and approved beatmaps
     */
    @JsonProperty("ranked_score")
    private long rankedScore;

    /**
     * Counts every score on ranked and approved beatmaps
     */
    @JsonProperty("total_score")
    private long totalScore;

    @JsonProperty("pp_rank")
    private int rank;

    private double level;

    @JsonProperty("pp_raw")
    private double pp;

    private double accuracy;

    /**
     * Counts for SS/S/A ranks on maps
     */
    @JsonProperty("count_rank_ss")
    private int countSS;

    /**
     * Counts for SS/S/A ranks on maps
     */
    @JsonProperty("count_rank_s")
    private int countS;

    /**
     * Counts for SS/S/A ranks on maps
     */
    @JsonProperty("count_rank_a")
    private int countA;

    private String country;

    @GameMode
    private int mode;

    public static <T extends OsuApiUser> T fromJsonObject(JsonNode o, Class<T> cls, @GameMode int mode)
            throws JsonProcessingException {
        T user = Downloader.JACKSON.treeToValue(o, cls);
        user.setMode(mode);
        return user;
    }
}
