package net.sistr.lmml.setup;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.SideChecker;
import net.sistr.lmml.client.resource.loader.LMSoundLoader;
import net.sistr.lmml.client.resource.loader.LMTextureLoader;
import net.sistr.lmml.client.resource.manager.LMSoundManager;
import net.sistr.lmml.entity.compound.IHasMultiModel;
import net.sistr.lmml.maidmodel.*;
import net.sistr.lmml.network.Networking;
import net.sistr.lmml.resource.loader.LMFileLoader;
import net.sistr.lmml.resource.manager.LMModelManager;
import net.sistr.lmml.resource.manager.LMTextureManager;

@Mod.EventBusSubscriber(modid = LittleMaidModelLoader.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {
        Networking.registerMessages();

        //Common->Clientの順のためこちらで処理
        if (SideChecker.isClient()) {
            LMFileLoader fileLoader = LMFileLoader.INSTANCE;
            LMTextureManager textureManager = LMTextureManager.INSTANCE;
            LMSoundManager soundManager = LMSoundManager.INSTANCE;
            LMTextureLoader textureProcessor = new LMTextureLoader(textureManager);
            textureProcessor.addPathConverter("assets/", "");
            textureProcessor.addPathConverter("mob/", "minecraft/textures/entity/");
            fileLoader.addLoader(textureProcessor);
            fileLoader.addLoader(new LMSoundLoader(soundManager));
        }

        //モデルを読み込む
        LMModelManager manager = LMModelManager.INSTANCE;
        manager.addModel("Default", ModelLittleMaid_Orign.class);
        manager.addModel("SR2", ModelLittleMaid_SR2.class);
        manager.addModel("Aug", ModelLittleMaid_Aug.class);
        manager.addModel("Archetype", ModelLittleMaid_Archetype.class);
        manager.addModel("Steve", ModelMulti_Steve.class);
        manager.addModel("Stef", ModelMulti_Stef.class);
        manager.setDefaultModel(manager.getModel("Default", IHasMultiModel.Layer.SKIN)
                .orElseThrow(RuntimeException::new));

        LMFileLoader.INSTANCE.load();

    }

}
