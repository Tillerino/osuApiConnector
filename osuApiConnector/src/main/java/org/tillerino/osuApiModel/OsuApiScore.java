package org.tillerino.osuApiModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.tillerino.osuApiModel.deserializer.CustomGson;
import org.tillerino.osuApiModel.deserializer.Date;
import org.tillerino.osuApiModel.deserializer.Skip;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

@Data
public class OsuApiScore {
	@SerializedName("beatmap_id")
	private int beatmapId;
	private long score;
	private int maxcombo;
	private int count300;
	private int count100;
	private int count50;
	private int countmiss;
	private int countkatu;
	private int countgeki;
	/**
	 * 1 = maximum combo of map reached; 0 otherwise
	 */
	private int perfect;
	/**
	 * bitwise flag representation of mods used. see reference
	 */
	@SerializedName("enabled_mods")
	private long mods;
	@SerializedName("user_id")
	private int userid;
	@Date
	private long date;
	private String rank;
	private double pp;
	
    static final Gson gson = CustomGson.wrap(false, OsuApiScore.class);
    
    @Skip
    private int mode;
    
    public static <T extends OsuApiScore> T fromJsonObject(JsonObject o, Class<T> cls, int mode) {
    	T score = gson.fromJson(o, cls);
    	score.setMode(mode);
		return score;
    }

	public static <T extends OsuApiScore> List<T> fromJsonArray(JsonArray jsonArray, Class<T> cls, int mode) {
		ArrayList<T> ret = new ArrayList<>();
		for(JsonElement elem : jsonArray) {
			ret.add(fromJsonObject((JsonObject) elem, cls, mode));
		}
		return ret;
	}
	
	public static final DecimalFormat percentage = new DecimalFormat("#.##%");

	public double getAccuracy() {
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
