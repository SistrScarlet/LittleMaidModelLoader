package net.sistr.lmml.network;

import net.blacklab.lmr.entity.maidmodel.IHasMultiModel;
import net.blacklab.lmr.entity.maidmodel.TextureBox;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.util.manager.ModelManager;

import java.util.function.Supplier;

//S2C
public class PacketSyncModel {
    private final int entityId;
    private final String model;
    private final String armor;
    private final byte color;
    private final boolean isContract;

    public PacketSyncModel(PacketBuffer buf) {
        entityId = buf.readInt();
        model = buf.readString();
        armor = buf.readString();
        color = buf.readByte();
        isContract = buf.readBoolean();
    }

    public PacketSyncModel(int entityId, String model, String armor, byte color, boolean isContract) {
        this.entityId = entityId;
        this.model = model;
        this.armor = armor;
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
            Entity entity = LittleMaidModelLoader.proxy.getClientWorld().getEntityByID(entityId);
            if (entity instanceof IHasMultiModel) {
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
