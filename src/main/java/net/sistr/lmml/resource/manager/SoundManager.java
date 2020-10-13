package net.sistr.lmml.resource.manager;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

//todo MixモードとFillモード - リソースパックスクリーン改造
public class SoundManager {
    private final Map<String, Map<String, ResourceLocation>> soundPaths = new HashMap<>();

    public void addSound(String packName, String fileName, ResourceLocation location) {
        Map<String, ResourceLocation> soundMap = soundPaths.computeIfAbsent(packName, k -> new HashMap<>());
        soundMap.put(fileName, location);
    }
}
