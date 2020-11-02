package net.sistr.lmml.client.resource.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sistr.lmml.client.LMSoundInstance;
import net.sistr.lmml.client.resource.holder.SoundHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//todo MixモードとFillモード - リソースパックスクリーン改造
@OnlyIn(Dist.CLIENT)
public class LMSoundManager {
    public static final LMSoundManager INSTANCE = new LMSoundManager();
    private final Map<String, SoundHolder> soundPaths = new HashMap<>();

    public void addSound(String packName, String fileName, ResourceLocation location) {
        SoundHolder soundHolder = soundPaths.computeIfAbsent(packName.toLowerCase(), k -> new SoundHolder(packName));
        soundHolder.addSound(fileName, location);
    }

    public Optional<SoundEventAccessor> getSound(String soundFileName) {
        int lastSplitter = soundFileName.lastIndexOf(".");
        if (lastSplitter == -1) {
            return Optional.empty();
        }
        String fileName = soundFileName.substring(lastSplitter + 1);
        String packName = soundFileName.substring(0, lastSplitter);
        lastSplitter = packName.lastIndexOf(".");
        if (lastSplitter != -1) {
            packName = packName.substring(lastSplitter + 1);
        }
        SoundHolder soundHolder = soundPaths.get(packName);
        if (soundHolder == null) {
            return Optional.empty();
        }
        return soundHolder.getSoundSet(fileName);
    }

    public void play(String soundFileName, SoundCategory soundCategory,
                     float volume, float pitch, double x, double y, double z) {
        getSound(soundFileName).ifPresent(soundSet -> Minecraft.getInstance().getSoundHandler()
                        .play(new LMSoundInstance(soundSet, soundCategory, volume, pitch, x, y, z)));
    }
}
