package net.sistr.lmml.client;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class LMSoundInstance implements ISound {
    private final SoundEventAccessor soundSet;
    private final Sound sound;
    private final ResourceLocation id;
    private final SoundCategory category;
    private final float volume;
    private final float pitch;
    private final double x;
    private final double y;
    private final double z;

    public LMSoundInstance(SoundEventAccessor soundSet, SoundCategory category,
                           float volume, float pitch, double x, double y, double z) {
        this.soundSet = soundSet;
        this.sound = soundSet.cloneEntry();
        this.id = sound.getSoundLocation();
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public ResourceLocation getSoundLocation() {
        return id;
    }

    @Nullable
    @Override
    public SoundEventAccessor createAccessor(SoundHandler handler) {
        return this.soundSet;
    }

    @Override
    public Sound getSound() {
        return this.sound;
    }

    @Override
    public SoundCategory getCategory() {
        return category;
    }

    @Override
    public boolean canRepeat() {
        return false;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public float getX() {
        return (float) x;
    }

    @Override
    public float getY() {
        return (float) y;
    }

    @Override
    public float getZ() {
        return (float) z;
    }

    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.LINEAR;
    }
}
