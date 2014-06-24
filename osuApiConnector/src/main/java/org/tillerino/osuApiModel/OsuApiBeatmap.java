package org.tillerino.osuApiModel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.tillerino.osuApiModel.deserializer.CustomGson;
import org.tillerino.osuApiModel.deserializer.Date;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

@Data
public class OsuApiBeatmap {
	@SerializedName("beatmap_id")
	public int id;
	@SerializedName("beatmapset_id")
	public int setId;
	public String artist;
	public String title;
	public String version;
	public String creator;
	public String source;
	/**
	 * 3 = qualified, 2 = approved, 1 = ranked, 0 = pending, -1 = WIP, -2 = graveyard
	 */
	public int approved;
	@Date
	@SerializedName("approved_date")
	public long approvedDate;
	@Date
	@SerializedName("last_update")
	public long lastUpdate;
	public double bpm; // can this be non-integral?
	/**
     * Star difficulty
     */
	@SerializedName("difficultyrating")
	public double starDifficulty;
    /**
     * Overall difficulty (OD)
     */
	@SerializedName("diff_overall")
	public int overallDifficulty;
    /**
     * Circle size value (CS)
     */
	@SerializedName("diff_size")
	public int circleSize;
    /**
     * Approach Rate (AR)
     */
	@SerializedName("diff_approach")
	public int approachRate;
    /**
     * Healthdrain (HP)
     */
	@SerializedName("diff_drain")
	public int healthDrain;
    /**
     * seconds from first note to last note not including breaks
     */
	@SerializedName("hit_length")
	public int hitLength;
	/**
     * seconds from first note to last note including breaks
     */
	@SerializedName("total_length")
	public int totalLength; 
	/**
     * mode (0 = osu!, 1 = Taiko, 2 = CtB, 3 = osu!mania)
     */
	public int mode;
    
    static final Gson gson = CustomGson.wrap(OsuApiBeatmap.class);
    
    public static <T extends OsuApiBeatmap> T fromJsonObject(JsonObject o, Class<T> cls) {
    	return gson.fromJson(o, cls);
    }

	public static <T extends OsuApiBeatmap> List<T> fromJsonArray(JsonArray jsonArray, Class<T> cls) {
		ArrayList<T> ret = new ArrayList<>();
		for(JsonElement elem : jsonArray) {
			ret.add(fromJsonObject((JsonObject) elem, cls));
		}
		return ret;
	}
}
