package net.sistr.lmml;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.sistr.lmml.client.LMMLPackFinder;
import net.sistr.lmml.config.LMMLConfig;
import net.sistr.lmml.resource.classloader.MultiModelClassLoader;
import net.sistr.lmml.resource.loader.LMConfigLoader;
import net.sistr.lmml.resource.loader.LMFileLoader;
import net.sistr.lmml.resource.loader.LMMultiModelLoader;
import net.sistr.lmml.resource.manager.LMConfigManager;
import net.sistr.lmml.resource.manager.LMModelManager;
import net.sistr.lmml.setup.ClientSetup;
import net.sistr.lmml.setup.ModSetup;
import net.sistr.lmml.setup.Registration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

@Mod("littlemaidmodelloader")
public class LittleMaidModelLoader {

    public static final String MODID = "littlemaidmodelloader";

    public static final Logger LOGGER = LogManager.getLogger();

    public LittleMaidModelLoader() {

        //このタイミングでないとうまく読み込まれない
        //また、後半のnullチェックはDataGen用
        if (FMLEnvironment.dist == Dist.CLIENT && Minecraft.getInstance() != null) {
            Minecraft.getInstance().getResourcePackList().addPackFinder(new LMMLPackFinder());
        }

        LMFileLoader fileLoader = LMFileLoader.INSTANCE;
        fileLoader.addLoadFolderPath(Paths.get("LMMLResources").toAbsolutePath());
        LMModelManager modelManager = LMModelManager.INSTANCE;
        LMConfigManager configManager = LMConfigManager.INSTANCE;
        fileLoader.addLoader(new LMMultiModelLoader(modelManager,
                new MultiModelClassLoader(fileLoader.getFolderPaths())));
        fileLoader.addLoader(new LMConfigLoader(configManager));

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LMMLConfig.COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, LMMLConfig.CLIENT_CONFIG);

        Registration.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);

    }

}
