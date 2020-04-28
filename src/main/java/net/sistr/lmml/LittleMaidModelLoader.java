package net.sistr.lmml;

import net.sistr.lmml.config.LMRConfig;
import net.sistr.lmml.setup.*;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("littlemaidmodelloader")
public class LittleMaidModelLoader {

    public static final String MODID = "littlemaidmodelloader";

    public static final Logger LOGGER = LogManager.getLogger();

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public LittleMaidModelLoader() {

        Registration.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);

    }

    public static void Debug(String pText, Object... pVals) {
        if (LMRConfig.cfg_PrintDebugMessage) {
            System.out.println(String.format("littleMaidMob-" + pText, pVals));
        }

    }

    public static void Debug(boolean isRemote, String format, Object... pVals) {
        Debug("Side=%s; ".concat(format), isRemote, pVals);
    }

}
