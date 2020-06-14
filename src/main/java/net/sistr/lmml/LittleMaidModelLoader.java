package net.sistr.lmml;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.sistr.lmml.config.LMMLConfig;
import net.sistr.lmml.setup.*;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.sistr.lmml.util.loader.LMMLPackFinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("littlemaidmodelloader")
public class LittleMaidModelLoader {

    public static final String MODID = "littlemaidmodelloader";

    public static final Logger LOGGER = LogManager.getLogger();

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public LittleMaidModelLoader() {

        if (FMLEnvironment.dist.isClient()) {
            //リソースをマイクラに読み込ませるための―詳しくはLMMLPackFinderにて
            Minecraft.getInstance().getResourcePackList().addPackFinder(new LMMLPackFinder());
        }

        Registration.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);

    }

    //あとで置き換える
    public static void Debug(String pText, Object... pVals) {
        if (LMMLConfig.cfg_PrintDebugMessage) {
            System.out.println(String.format("littleMaidMob-" + pText, pVals));
        }

    }

}
