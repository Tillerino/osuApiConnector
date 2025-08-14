package org.tillerino.osuApiModel.v2;

public class OsuApiUserV2 {
    public int id;
    public String username;
    public int modeInt;

    public Country country;
    public Statistics statistics;

    public static class Country {
        public String code;
    }

    public static class Statistics {
        public int count_300;
        public int count_100;
        public int count_50;
        public int play_count;
        public long ranked_score;
        public long total_score;
        public int global_rank;
        public double pp;
        public double hit_accuracy;

        public Level level;
        public GradeCounts grade_counts;

        public static class Level {
            public double current;
        }

        public static class GradeCounts {
            public int ss;
            public int s;
            public int a;
        }
    }
}
