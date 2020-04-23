package net.blacklab.lmr.setup;

import net.blacklab.lmr.LittleMaidReengaged;
import net.blacklab.lmr.util.loader.LMFileLoader;
import net.blacklab.lmr.util.manager.ModelManager;
import net.blacklab.lmr.util.manager.SoundManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = LittleMaidReengaged.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(final FMLCommonSetupEvent event) {
        //リトルメイドファイルローダー
        LMFileLoader.instance.load();

        //マルチモデルセットアップ
        ModelManager.instance.createLittleMaidModels();

        //サウンドパックセットアップ
        SoundManager.instance.createSounds();


    }
}
