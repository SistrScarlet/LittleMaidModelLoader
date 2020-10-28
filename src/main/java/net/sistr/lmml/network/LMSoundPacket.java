package net.sistr.lmml.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.sistr.lmml.entity.compound.SoundPlayable;

import java.util.function.Supplier;

public class LMSoundPacket {
    private final int entityId;
    private final String soundName;

    public LMSoundPacket(PacketBuffer buf) {
        entityId = buf.readVarInt();
        soundName = buf.readString(32767);
    }

    public LMSoundPacket(Entity entity, String soundName) {
        entityId = entity.getEntityId();
        this.soundName = soundName;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(entityId);
        buf.writeString(soundName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = Minecraft.getInstance().player;
            if (player == null)
                return;
            receiveS2CPacket(entityId, soundName, player);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sendS2CPacket(Entity entity, String soundName) {
        Networking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new LMSoundPacket(entity, soundName));
    }

    @OnlyIn(Dist.CLIENT)
    private void receiveS2CPacket(int entityId, String soundName, PlayerEntity player) {
        Entity entity = player.world.getEntityByID(entityId);
        if (entity instanceof SoundPlayable) {
            ((SoundPlayable) entity).play(soundName);
        }
    }

}
