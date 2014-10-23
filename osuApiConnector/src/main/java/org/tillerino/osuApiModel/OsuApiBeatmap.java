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
import lombok.Getter;
import lombok.Setter;

import org.tillerino.osuApiModel.deserializer.CustomGson;
import org.tillerino.osuApiModel.deserializer.Date;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BeatmapSetId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

@Data
public class OsuApiBeatmap {
	@BeatmapId
	@Getter(onMethod=@__(@BeatmapId))
	@Setter(onParam=@__(@BeatmapId))
	@SerializedName("beatmap_id")
	private int beatmapId;
	
	@BeatmapSetId
	@Getter(onMethod=@__(@BeatmapSetId))
	@Setter(onParam=@__(@BeatmapSetId))
	@SerializedName("beatmapset_id")
	private int setId;
	
	private String artist;
	private String title;
	private String version;
	private String creator;
	private String source;
	/**
	 * 3 = qualified, 2 = approved, 1 = ranked, 0 = pending, -1 = WIP, -2 = graveyard
	 */
	private int approved;
	
	/**
	 * may be null if not ranked
	 */
	@Date
	@MillisSinceEpoch
	@Getter(onMethod=@__(@MillisSinceEpoch))
	@Setter(onParam=@__(@MillisSinceEpoch))
	@SerializedName("approved_date")
	private Long approvedDate;
	
	@Date
	@MillisSinceEpoch
	@Getter(onMethod=@__(@MillisSinceEpoch))
	@Setter(onParam=@__(@MillisSinceEpoch))
	@SerializedName("last_update")
	private long lastUpdate;
	
	private double bpm; // can this be non-integral?
	
	/**
     * Star difficulty
     */
	@SerializedName("difficultyrating")
	private double starDifficulty;
	
    /**
     * Overall difficulty (OD)
     */
	@SerializedName("diff_overall")
	private double overallDifficulty;
	
    /**
     * Circle size value (CS)
     */
	@SerializedName("diff_size")
	private double circleSize;
	
    /**
     * Approach Rate (AR)
     */
	@SerializedName("diff_approach")
	private double approachRate;
	
    /**
     * Healthdrain (HP)
     */
	@SerializedName("diff_drain")
	private double healthDrain;
	
    /**
     * seconds from first note to last note not including breaks
     */
	@SerializedName("hit_length")
	private int hitLength;
	
	/**
     * seconds from first note to last note including breaks
     */
	@SerializedName("total_length")
	private int totalLength; 
	
	/**
     * mode (0 = osu!, 1 = Taiko, 2 = CtB, 3 = osu!mania)
     */
	@GameMode
	@Getter(onMethod=@__(@GameMode))
	@Setter(onParam=@__(@GameMode))
	private int mode;
    
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

	public static double calcAR(double ar, @BitwiseMods long mods) {
		if(Easy.is(mods)) {
			ar /= 2;
		}
		if(HardRock.is(mods)) {
			ar = min(10, ar * 1.4);
		}
		if(DoubleTime.is(mods) || Nightcore.is(mods)) {
			ar = msToAr(arToMs(ar) * 2 / 3d);
		}
		if(HalfTime.is(mods)) {
			ar = msToAr(arToMs(ar) * 4 / 3d);
		}
		return ar;
	}

	public static double calcOd(double od, @BitwiseMods long mods) {
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
	
	public static double calcBpm(double bpm, @BitwiseMods long mods) {
		if(DoubleTime.is(mods))
			bpm *= 1.5;
		if(HalfTime.is(mods))
			bpm *= 0.75;
		return bpm;
	}
	
	public static int calcTotalLength(int totalLength, @BitwiseMods long mods) {
		if(DoubleTime.is(mods))
			totalLength = (int) (totalLength * 2D/3);
		if(HalfTime.is(mods))
			totalLength = (int) (totalLength * 4D/3);
		return totalLength;
	}
	
	public double getApproachRate(@BitwiseMods long mods) {
		return calcAR(getApproachRate(), mods);
	}
	
	public double getOverallDifficulty(@BitwiseMods long mods) {
		return calcOd(getOverallDifficulty(), mods);
	}
	
	public double getBpm(@BitwiseMods long mods) {
		return calcBpm(getBpm(), mods);
	}
	
	public int getTotalLength(@BitwiseMods long mods) {
		return calcTotalLength(getTotalLength(), mods);
	}
}
