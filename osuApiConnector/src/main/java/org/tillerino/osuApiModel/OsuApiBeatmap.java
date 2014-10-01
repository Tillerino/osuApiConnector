package org.tillerino.osuApiModel;

import static java.lang.Math.min;
import static org.tillerino.osuApiModel.Mods.DoubleTime;
import static org.tillerino.osuApiModel.Mods.Easy;
import static org.tillerino.osuApiModel.Mods.HalfTime;
import static org.tillerino.osuApiModel.Mods.HardRock;
import static org.tillerino.osuApiModel.Mods.Nightcore;

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
	/**
	 * may be null if not ranked
	 */
	public Long approvedDate;
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
	public double overallDifficulty;
    /**
     * Circle size value (CS)
     */
	@SerializedName("diff_size")
	public double circleSize;
    /**
     * Approach Rate (AR)
     */
	@SerializedName("diff_approach")
	public double approachRate;
    /**
     * Healthdrain (HP)
     */
	@SerializedName("diff_drain")
	public double healthDrain;
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
    
    static final Gson gson = CustomGson.wrap(false, OsuApiBeatmap.class);
    
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

	public static double arToMs(double ar) {
		if(ar < 5)
			return 1800 - ar * 120;
		return 1200 - 150 * (ar - 5);
	}

	public static double msToAr(double ms) {
		if(ms > 1200)
			return (1800 - ms) / 120;
		
		return (1200 - ms) / 150 + 5;
	}

	public static double odToMs(double od) {
		return 80 - 6 * od;
	}

	public static double msToOd(double ms) {
		return (80 - ms) / 6;
	}

	public static double calcAR(double ar, long mods) {
		if(Easy.is(mods)) {
			ar /= 2;
		}
		if(HardRock.is(mods)) {
			ar = min(10, ar * 1.4);
		}
		if(DoubleTime.is(mods) || Nightcore.is(mods)) {
			ar = msToAr(arToMs(ar) * 2 / 3);
		}
		if(HalfTime.is(mods)) {
			ar = msToAr(arToMs(ar) * 1.5);
		}
		return ar;
	}

	public static double calcOd(double od, long mods) {
		if(Easy.is(mods)) {
			od /= 2;
		}
		if(HardRock.is(mods)) {
			od = min(10, od * 1.4);
		}
		if(DoubleTime.is(mods) || Nightcore.is(mods)) {
			od = msToOd(odToMs(od) * 2 / 3);
		}
		if(HalfTime.is(mods)) {
			od = msToOd(odToMs(od) * 1.5);
		}
		return od;
	}
	
	public static double calcBpm(double bpm, long mods) {
		if(DoubleTime.is(mods))
			bpm *= 1.5;
		if(HalfTime.is(mods))
			bpm *= 0.75;
		return bpm;
	}
	
	public static int calcTotalLength(int totalLength, long mods) {
		if(DoubleTime.is(mods))
			totalLength = (int) (totalLength * 2D/3);
		if(HalfTime.is(mods))
			totalLength = (int) (totalLength * 4D/3);
		return totalLength;
	}
	
	public double getApproachRate(long mods) {
		return calcAR(getApproachRate(), mods);
	}
	
	public double getOverallDifficulty(long mods) {
		return calcOd(getOverallDifficulty(), mods);
	}
	
	public double getBpm(long mods) {
		return calcBpm(getBpm(), mods);
	}
	
	public int getTotalLength(long mods) {
		return calcTotalLength(getTotalLength(), mods);
	}
}
