package net.sistr.lmml;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.sistr.lmml.config.LMMLConfig;
import net.sistr.lmml.resource.LMMLPackFinder;
import net.sistr.lmml.resource.MultiModelClassLoader;
import net.sistr.lmml.resource.loader.*;
import net.sistr.lmml.setup.ClientSetup;
import net.sistr.lmml.setup.ModSetup;
import net.sistr.lmml.setup.Registration;
import net.sistr.lmml.resource.manager.ConfigManager;
import net.sistr.lmml.resource.manager.ModelManager;
import net.sistr.lmml.resource.manager.SoundManager;
import net.sistr.lmml.resource.manager.TextureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

@Mod("littlemaidmodelloader")
public class LittleMaidModelLoader {

    private static LittleMaidModelLoader instance;
    private final FileLoader fileLoader;
    private final TextureManager textureManager;
    private final ModelManager modelManager;
    private final SoundManager soundManager;
    private final ConfigManager configManager;

    public static final String MODID = "littlemaidmodelloader";

    public static final Logger LOGGER = LogManager.getLogger();

    public LittleMaidModelLoader() {
        instance = this;

        //このタイミングでないとうまく読み込まれない
        //また、後半のnullチェックはDataGen用
        if (FMLEnvironment.dist == Dist.CLIENT && Minecraft.getInstance() != null) {
            Minecraft.getInstance().getResourcePackList().addPackFinder(new LMMLPackFinder());
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LMMLConfig.COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, LMMLConfig.CLIENT_CONFIG);

        textureManager = new TextureManager();
        modelManager = new ModelManager();
        soundManager = new SoundManager();
        configManager = new ConfigManager();
        fileLoader = new FileLoader();
        fileLoader.addLoadFolderPath(Paths.get(FMLPaths.GAMEDIR.get().toString(), "/LMMLResources"));
        TextureLoader textureProcessor = new TextureLoader(textureManager);
        textureProcessor.addPathConverter("assets/", "");
        textureProcessor.addPathConverter("mob/", "minecraft/textures/entity/");
        fileLoader.addProcessor(textureProcessor);
        fileLoader.addProcessor(new MultiModelLoader(modelManager, new MultiModelClassLoader(fileLoader.getFolderPaths())));
        fileLoader.addProcessor(new SoundLoader(soundManager));
        fileLoader.addProcessor(new ConfigLoader(configManager));

        Registration.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);

    }

    public static LittleMaidModelLoader getInstance() {
        return instance;
    }

    public FileLoader getFileLoader() {
        return fileLoader;
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public ModelManager getModelManager() {
        return modelManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

}
