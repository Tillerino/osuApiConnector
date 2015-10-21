package org.tillerino.osuApiModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.tillerino.osuApiModel.deserializer.CustomGson;
import org.tillerino.osuApiModel.deserializer.Date;
import org.tillerino.osuApiModel.deserializer.Skip;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;
import org.tillerino.osuApiModel.types.UserId;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

@Data
public class OsuApiScore {
	@SerializedName("beatmap_id")
	@BeatmapId
	@Getter(onMethod=@__(@BeatmapId))
	@Setter(onParam=@__(@BeatmapId))
	private int beatmapId;
	
	private long score;
	
	@SerializedName("maxcombo")
	private int maxCombo;
	
	private int count300;
	private int count100;
	private int count50;
	
	@SerializedName("countmiss")
	private int countMiss;
	
	@SerializedName("countkatu")
	private int countKatu;
	
	@SerializedName("countgeki")
	private int countGeki;
	
	/**
	 * 1 = maximum combo of map reached; 0 otherwise
	 */
	private int perfect;
	/**
	 * bitwise flag representation of mods used. see reference
	 */
	@SerializedName("enabled_mods")
	@BitwiseMods
	@Getter(onMethod=@__(@BitwiseMods))
	@Setter(onParam=@__(@BitwiseMods))
	private long mods;
	
	@SerializedName("user_id")
	@UserId
	@Getter(onMethod=@__(@UserId))
	@Setter(onParam=@__(@UserId))
	private int userId;
	
	@Date
	@MillisSinceEpoch
	@Getter(onMethod=@__(@MillisSinceEpoch))
	@Setter(onParam=@__(@MillisSinceEpoch))
	private long date;
	
	private String rank;
	
	@Skip
	@Getter(onMethod=@__(@CheckForNull))
	private Double pp = null;
	
    static final Gson gson = CustomGson.wrap(false, OsuApiScore.class);
    
    @Skip
	@GameMode
	@Getter(onMethod=@__(@GameMode))
	@Setter(onParam=@__(@GameMode))
    private int mode;
    
    public static <T extends OsuApiScore> T fromJsonObject(JsonObject o, Class<T> cls, @GameMode int mode) {
    	T score = gson.fromJson(o, cls);
    	score.setMode(mode);
    	if(o.has("pp")) {
    		JsonElement ppMaybe = o.get("pp");
    		if(!ppMaybe.isJsonNull()) {
    			score.setPp(ppMaybe.getAsDouble());
    		}
    	}
		return score;
    }

	public static <T extends OsuApiScore> List<T> fromJsonArray(JsonArray jsonArray, Class<T> cls, @GameMode int mode) {
		ArrayList<T> ret = new ArrayList<>();
		for(JsonElement elem : jsonArray) {
			ret.add(fromJsonObject((JsonObject) elem, cls, mode));
		}
		return ret;
	}
	
	public static final DecimalFormat percentage = new DecimalFormat("#.##%");

	public double getAccuracy() {
		return getAccuracy(count300, count100, count50, countMiss);
	}
	
	public static double getAccuracy(double count300, double count100, double count50, double countmiss) {
		double sum = 0d;

		sum += count50 * 50;
		sum += count100 * 100;
		sum += count300 * 300;

		double denom = (count50 + count100 + count300 + countmiss) * 300;

		return sum / denom;
	}

	public List<Mods> getModsList() {
		return Mods.getMods(getMods());
	}

	public String getPercentagePretty() {
		return percentage.format(getAccuracy());
	}
}
