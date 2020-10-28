package net.sistr.lmml.client.resource.holder;

import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class SoundHolder {
    private final String name;

    private final Map<String, SoundEventAccessor> sounds = new HashMap<>();

    public SoundHolder(String name) {
        this.name = name;
    }

    public void addSound(String fileName, ResourceLocation resource) {
        SoundEventAccessor soundSet = sounds.computeIfAbsent(fileName.toLowerCase(), k ->
                new SoundEventAccessor(resource, resource.toString().replace("/", ".")));
        soundSet.addSound(new Sound(resource.toString(), 1F, 1F, 1, Sound.Type.FILE,
                false, false, 16) {
            @Override
            public ResourceLocation getSoundAsOggLocation() {
                return super.getSoundLocation();
            }
        });
    }

    public String getName() {
        return name;
    }

    public Optional<SoundEventAccessor> getSoundSet(String fileName) {
        return Optional.ofNullable(sounds.get(fileName));
    }

}
