package net.sistr.lmml.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.sistr.lmml.entity.compound.IHasMultiModel;
import net.sistr.lmml.resource.manager.LMTextureManager;
import net.sistr.lmml.resource.util.ArmorSets;
import net.sistr.lmml.resource.util.TextureColors;

import java.util.function.Supplier;

public class SyncMultiModelPacket {
    private final int entityId;
    private final String textureName;
    private final ArmorSets<String> armorTextureName = new ArmorSets<>();
    private final TextureColors color;
    private final boolean isContract;

    public SyncMultiModelPacket(PacketBuffer buf) {
        entityId = buf.readVarInt();
        textureName = buf.readString(32767);
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            armorTextureName.setArmor(buf.readString(32767), part);
        }
        color = buf.readEnumValue(TextureColors.class);
        isContract = buf.readBoolean();
    }

    public SyncMultiModelPacket(Entity entity, IHasMultiModel hasMultiModel) {
        entityId = entity.getEntityId();
        textureName = hasMultiModel.getTextureHolder(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD)
                .getTextureName();
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            armorTextureName.setArmor(hasMultiModel.getTextureHolder(IHasMultiModel.Layer.INNER, part)
                    .getTextureName(), part);
        }
        color = hasMultiModel.getColor();
        isContract = hasMultiModel.isContract();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(entityId);
        buf.writeString(textureName);
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            buf.writeString(armorTextureName.getArmor(part).orElseThrow(IllegalArgumentException::new));
        }
        buf.writeEnumValue(color);
        buf.writeBoolean(isContract);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                applyMultiModelClient(entityId, textureName, armorTextureName, color, isContract);
            } else {
                PlayerEntity player = ctx.get().getSender();
                if (player == null) {
                    return;
                }
                applyMultiModelServer(entityId, textureName, armorTextureName, color, isContract, player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendC2SPacket(Entity entity, IHasMultiModel hasMultiModel) {
        Networking.INSTANCE.sendToServer(new SyncMultiModelPacket(entity, hasMultiModel));
    }

    public static void sendS2CPacket(Entity entity, IHasMultiModel hasMultiModel) {
        Networking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new SyncMultiModelPacket(entity, hasMultiModel));
    }

    @OnlyIn(Dist.CLIENT)
    public static void applyMultiModelClient(int entityId, String textureName, ArmorSets<String> armorTextureName,
                                             TextureColors color, boolean isContract) {
        World world = Minecraft.getInstance().world;
        if (world == null) return;
        Entity entity = world.getEntityByID(entityId);
        if (!(entity instanceof IHasMultiModel)) return;
        IHasMultiModel multiModel = (IHasMultiModel) entity;
        multiModel.setColor(color);
        multiModel.setContract(isContract);
        LMTextureManager textureManager = LMTextureManager.INSTANCE;
        textureManager.getTexture(textureName).filter(textureHolder ->
                multiModel.isAllowChangeTexture(entity, textureHolder, IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD))
                .ifPresent(textureHolder -> multiModel.setTextureHolder(textureHolder, IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD));
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            String armorName = armorTextureName.getArmor(part)
                    .orElseThrow(() -> new IllegalStateException("テクスチャが存在しません。"));
            textureManager.getTexture(armorName).filter(textureHolder ->
                    multiModel.isAllowChangeTexture(entity, textureHolder, IHasMultiModel.Layer.INNER, part))
                    .ifPresent(textureHolder -> multiModel.setTextureHolder(textureHolder, IHasMultiModel.Layer.INNER, part));
        }
    }

    public static void applyMultiModelServer(int entityId, String textureName, ArmorSets<String> armorTextureName, TextureColors color,
                                             boolean isContract, PlayerEntity player) {
        Entity entity = player.world.getEntityByID(entityId);
        if (!(entity instanceof IHasMultiModel)) return;
        IHasMultiModel multiModel = (IHasMultiModel) entity;
        multiModel.setColor(color);
        multiModel.setContract(isContract);
        LMTextureManager textureManager = LMTextureManager.INSTANCE;
        textureManager.getTexture(textureName).filter(textureHolder ->
                multiModel.isAllowChangeTexture(entity, textureHolder, IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD))
                .ifPresent(textureHolder -> multiModel.setTextureHolder(textureHolder, IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD));
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            String armorName = armorTextureName.getArmor(part)
                    .orElseThrow(() -> new IllegalStateException("テクスチャが存在しません。"));
            textureManager.getTexture(armorName).filter(textureHolder ->
                    multiModel.isAllowChangeTexture(entity, textureHolder, IHasMultiModel.Layer.INNER, part))
                    .ifPresent(textureHolder -> multiModel.setTextureHolder(textureHolder, IHasMultiModel.Layer.INNER, part));
        }
        sendS2CPacket(entity, multiModel);
    }

}
