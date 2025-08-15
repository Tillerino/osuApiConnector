package org.tillerino.osuApiModel.v2;

record OsuApiBeatmapV2(
        int id,
        int beatmapset_id,
        int user_id,
        String version,
        String status,
        String last_updated,
        double bpm,
        double difficulty_rating,
        double accuracy,
        double ar,
        double cs,
        double drain,
        int hit_length,
        int total_length,
        int mode_int,
        String checksum,
        int playcount,
        int passcount,
        int max_combo,
        Beatmapset beatmapset,
        Attributes attributes) {

    record Beatmapset(
            String title,
            String artist,
            String source,
            String creator,
            String tags,
            int genre_id,
            int language_id,
            String ranked_date,
            int favourite_count) {}

    record Attributes(double aim_difficulty, double speed_difficulty) {}
}
