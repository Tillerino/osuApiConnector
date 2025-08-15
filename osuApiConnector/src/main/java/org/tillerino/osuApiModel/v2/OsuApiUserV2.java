package org.tillerino.osuApiModel.v2;

record OsuApiUserV2(int id, String username, int modeInt, Country country, Statistics statistics) {

    record Country(String code) {}

    record Statistics(
            int count_300,
            int count_100,
            int count_50,
            int play_count,
            long ranked_score,
            long total_score,
            int global_rank,
            double pp,
            double hit_accuracy,
            Level level,
            GradeCounts grade_counts) {

        record Level(double current) {}

        record GradeCounts(int ss, int s, int a) {}
    }
}
