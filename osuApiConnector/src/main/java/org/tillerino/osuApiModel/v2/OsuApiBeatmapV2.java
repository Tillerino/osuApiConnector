package org.tillerino.osuApiModel.v2;

import lombok.Data;

@Data
public class OsuApiBeatmapV2 {
    private int id;
    private int beatmapset_id;
    private int user_id;
    private String version;
    private String status;
    private String last_updated;
    private double bpm;
    private double difficulty_rating;
    private double accuracy;
    private double ar;
    private double cs;
    private double drain;
    private int hit_length;
    private int total_length;
    private int mode_int;
    private String checksum;
    private int playcount;
    private int passcount;
    private int max_combo;

    private Beatmapset beatmapset;
    private Attributes attributes;

    @Data
    public static class Beatmapset {
        private String title;
        private String artist;
        private String creator;
        private String tags;
        private int genre_id;
        private int language_id;
        private String ranked_date;
        private int favourite_count;
    }

    @Data
    public static class Attributes {
        private double aim_difficulty;
        private double speed_difficulty;
    }
}
