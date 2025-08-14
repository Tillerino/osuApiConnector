package org.tillerino.osuApiModel.v2;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class OsuApiScoreBeatmapV2 {
    private int beatmap_id;
    private long score;
    private int max_combo;
    private boolean perfect;
    private JsonNode mods;
    private int user_id;
    private String created_at;
    private String rank;
    private Double pp;
    private int mode_int;

    private Statistics statistics;

    @Data
    public static class Statistics {
        private int count_300;
        private int count_100;
        private int count_50;
        private int count_miss;
        private int count_katu;
        private int count_geki;
    }
}
