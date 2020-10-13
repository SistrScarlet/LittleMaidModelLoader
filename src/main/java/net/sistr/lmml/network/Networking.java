package net.sistr.lmml.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.sistr.lmml.LittleMaidModelLoader;

public class Networking {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;

    public static int nextID() {
        return ID++;
    }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(LittleMaidModelLoader.MODID, "littlemaidmodelloader"), () -> "1.0", s -> true, s -> true);

        INSTANCE.registerMessage(nextID(),
                PacketSyncMultiModel.class,
                PacketSyncMultiModel::toBytes,
                PacketSyncMultiModel::new,
                PacketSyncMultiModel::handle);
    }
}
