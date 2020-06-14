package net.sistr.lmml.network;

import net.blacklab.lmr.entity.maidmodel.IHasMultiModel;
import net.blacklab.lmr.entity.maidmodel.TextureBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.util.manager.ModelManager;

import javax.annotation.Nullable;
import java.util.function.Supplier;

//S2C & C2S
//つまり双方向で使える。すごいね！
//でもノーチェックなのでチートクライアントで自分のメイドの色を勝手に変られたりする…かも？
public class PacketSyncModel {
    private final int entityId;
    private final String model;
    private final String armor;
    private final byte color;
    private final boolean isContract;

    public PacketSyncModel(PacketBuffer buf) {
        entityId = buf.readInt();
        model = buf.readString(32767);
        armor = buf.readString(32767);
        color = buf.readByte();
        isContract = buf.readBoolean();
    }

    public PacketSyncModel(int entityId, @Nullable String model, @Nullable String armor, byte color, boolean isContract) {
        this.entityId = entityId;
        if (model == null) {
            this.model = "default";
        } else {
            this.model = model;
        }
        if (model == null) {
            this.armor = "default";
        } else {
            this.armor = armor;
        }
        this.color = color;
        this.isContract = isContract;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(entityId);
        buf.writeString(model);
        buf.writeString(armor);
        buf.writeByte(color);
        buf.writeBoolean(isContract);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (player == null) {
                player = LittleMaidModelLoader.proxy.getClientPlayer();
                if (player == null) {
                    return;
                }
            }
            Entity entity = player.world.getEntityByID(entityId);
            if (entity instanceof IHasMultiModel) {
                //距離が遠い場合はキャンセル。簡易すぎるチェック
                if (16 * 16 < entity.getDistanceSq(player)) {
                    return;
                }
                TextureBox mainModel = ModelManager.instance.getTextureBox(model);
                if (mainModel != null) {
                    ((IHasMultiModel) entity).setTextureBox(0, mainModel);
                }
                TextureBox armorModel = ModelManager.instance.getTextureBox(armor);
                if (armorModel != null) {
                    ((IHasMultiModel) entity).setTextureBox(1, armorModel);
                }
                ((IHasMultiModel) entity).setColor(color);
                ((IHasMultiModel) entity).setContract(isContract);
                ((IHasMultiModel) entity).updateTextures();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
