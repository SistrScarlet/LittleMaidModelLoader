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
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.entity.IHasMultiModel;
import net.sistr.lmml.util.ArmorsHolder;
import net.sistr.lmml.util.TextureColor;
import net.sistr.lmml.resource.manager.TextureManager;

import java.util.function.Supplier;

//S2C & C2S
public class PacketSyncMultiModel {
    private final int entityId;
    private final String textureName;
    private final ArmorsHolder<String> armorTextureName;
    private final TextureColor color;
    private final boolean isContract;

    public PacketSyncMultiModel(PacketBuffer buf) {
        entityId = buf.readInt();
        textureName = buf.readString(32767);
        armorTextureName = new ArmorsHolder<>();
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            armorTextureName.setArmor(buf.readString(32767), part);
        }
        color = buf.readEnumValue(TextureColor.class);
        isContract = buf.readBoolean();
    }

    public PacketSyncMultiModel(int entityId, String textureName, ArmorsHolder<String> armorTextureName,
                                TextureColor color, boolean isContract) {
        this.entityId = entityId;
        this.textureName = textureName;
        this.armorTextureName = armorTextureName;
        this.color = color;
        this.isContract = isContract;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(entityId);
        buf.writeString(textureName);
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            buf.writeString(armorTextureName.getArmor(part)
                    .orElseThrow(() -> new IllegalStateException("テクスチャ名が存在しません。")));
        }
        buf.writeEnumValue(color);
        buf.writeBoolean(isContract);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = null;
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                world = getWorld();
            } else {
                PlayerEntity player = ctx.get().getSender();
                if (player != null) {
                    world = player.world;
                    Networking.INSTANCE.send(PacketDistributor.DIMENSION.with(() -> player.dimension), this);
                }
            }
            if (world == null) return;
            Entity entity = world.getEntityByID(entityId);
            if (!(entity instanceof IHasMultiModel)) return;
            IHasMultiModel multiModel = (IHasMultiModel) entity;
            multiModel.setColor(color);
            multiModel.setContract(isContract);
            TextureManager textureManager = LittleMaidModelLoader.getInstance().getTextureManager();
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
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public World getWorld() {
        return Minecraft.getInstance().world;
    }
}
