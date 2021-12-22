package org.tillerino.osuApiModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.tillerino.osuApiModel.deserializer.DateToLong;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;
import org.tillerino.osuApiModel.types.UserId;

@Data
public class OsuApiScore {
	@JsonProperty("beatmap_id")
	@BeatmapId
	@Getter(onMethod=@__(@BeatmapId))
	@Setter(onParam=@__(@BeatmapId))
	private int beatmapId;
	
	private long score;
	
	@JsonProperty("maxcombo")
	private int maxCombo;
	
	private int count300;
	private int count100;
	private int count50;
	
	@JsonProperty("countmiss")
	private int countMiss;
	
	@JsonProperty("countkatu")
	private int countKatu;
	
	@JsonProperty("countgeki")
	private int countGeki;
	
	/**
	 * 1 = maximum combo of map reached; 0 otherwise
	 */
	private int perfect;
	/**
	 * bitwise flag representation of mods used. see reference
	 */
	@JsonProperty("enabled_mods")
	@BitwiseMods
	@Getter(onMethod=@__(@BitwiseMods))
	@Setter(onParam=@__(@BitwiseMods))
	private long mods;
	
	@JsonProperty("user_id")
	@UserId
	@Getter(onMethod=@__(@UserId))
	@Setter(onParam=@__(@UserId))
	private int userId;

	@JsonDeserialize(using = DateToLong.class)
	@MillisSinceEpoch
	@Getter(onMethod=@__(@MillisSinceEpoch))
	@Setter(onParam=@__(@MillisSinceEpoch))
	private long date;
	
	private String rank;
	
	@Getter(onMethod=@__(@CheckForNull))
	private Double pp = null;
	
    @GameMode
	@Getter(onMethod=@__(@GameMode))
	@Setter(onParam=@__(@GameMode))
    private int mode;
    
    public static <T extends OsuApiScore> T fromJsonObject(JsonNode o, Class<T> cls, @GameMode int mode) throws JsonProcessingException {
    	T score = Downloader.JACKSON.treeToValue(o, cls);
    	score.setMode(mode);
		return score;
    }

	public static <T extends OsuApiScore> List<T> fromJsonArray(ArrayNode jsonArray, Class<T> cls, @GameMode int mode) throws JsonProcessingException {
		ArrayList<T> ret = new ArrayList<>();
		for(JsonNode elem : jsonArray) {
			ret.add(fromJsonObject(elem, cls, mode));
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
