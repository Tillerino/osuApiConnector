package org.tillerino.osuApiModel.deserializer;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

public class BitwiseToMods {

    private static final Map<String, Integer> osuMods = new LinkedHashMap<>();

    static {
        osuMods.put("NF", 1);
        osuMods.put("EZ", 2);
        osuMods.put("HD", 8);
        osuMods.put("HR", 16);
        osuMods.put("SD", 32);
        osuMods.put("DT", 64);
        osuMods.put("RL", 128);
        osuMods.put("HT", 256);
        osuMods.put("NC", 512);
        osuMods.put("FL", 1024);
        osuMods.put("AT", 2048);
        osuMods.put("SO", 4096);
        osuMods.put("AP", 8192);
        osuMods.put("PF", 16384);
    }

    public static int modsArrayToBitwise(JsonNode modsArray) {
        int flag = 0;
        if (modsArray != null && modsArray.isArray()) {
            for (JsonNode modNode : modsArray) {
                String mod = modNode.asText();
                Integer modValue = osuMods.get(mod);
                if (modValue != null) {
                    flag |= modValue; // bitwise OR to accumulate flags
                }
            }
        }
        return flag;
    }

    public static List<String> bitwiseToModsArray(int bitwise) {
        List<String> mods = new ArrayList<>();
        if (bitwise == 0) {
            mods.add("NM");
            return mods;
        }

        for (Map.Entry<String, Integer> entry : osuMods.entrySet()) {
            if ((bitwise & entry.getValue()) != 0) {
                mods.add(entry.getKey());
            }
        }
        return mods;
    }
}
