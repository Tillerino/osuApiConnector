package org.tillerino.osuApiModel;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.tillerino.osuApiModel.deserializer.CustomGson;
import org.tillerino.osuApiModel.deserializer.Skip;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.UserId;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

@Data
public class OsuApiUser {
	@SerializedName("user_id")
	@UserId
	@Getter(onMethod=@__(@UserId))
	@Setter(onParam=@__(@UserId))
	private int userId;
	
	@SerializedName("username")
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
	@SerializedName("playcount")
	private int playCount;
	/**
	 * Counts the best individual score on each ranked and approved beatmaps
	 */
	@SerializedName("ranked_score")
	private long rankedScore;
	
	/**
	 * Counts every score on ranked and approved beatmaps
	 */
	@SerializedName("total_score")
	private long totalScore;
	
	@SerializedName("pp_rank")
	private int rank;
	
	private double level;
	
	@SerializedName("pp_raw")
	private double pp;
	
	private double accuracy;
	
	/**
	 * Counts for SS/S/A ranks on maps
	 */
	@SerializedName("count_rank_ss")
	private int countSS;
	
	/**
	 * Counts for SS/S/A ranks on maps
	 */
	@SerializedName("count_rank_s")
	private int countS;
	
	/**
	 * Counts for SS/S/A ranks on maps
	 */
	@SerializedName("count_rank_a")
	private int countA;
	
	private String country;
	
	@Skip
	@GameMode
	@Getter(onMethod=@__(@GameMode))
	@Setter(onParam=@__(@GameMode))
	private int mode;
	
	static final Gson gson = CustomGson.wrap(false, OsuApiUser.class);
    
    public static <T extends OsuApiUser> T fromJsonObject(JsonObject o, Class<T> cls, @GameMode int mode) {
    	T user = gson.fromJson(o, cls);
    	user.setMode(mode);
		return user;
    }
}
