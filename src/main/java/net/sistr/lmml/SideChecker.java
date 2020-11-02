package net.sistr.lmml;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

//Fabric版との歩調合わせ
public class SideChecker {
    private static final Side side = FMLEnvironment.dist == Dist.CLIENT ? Side.CLIENT : Side.SERVER;

    public static Side getSide() {
        return side;
    }

    public static boolean isClient() {
        return side == Side.CLIENT;
    }

    public static boolean isServer() {
        return side == Side.SERVER;
    }

    public enum Side {
        CLIENT,
        SERVER;
    }

}
