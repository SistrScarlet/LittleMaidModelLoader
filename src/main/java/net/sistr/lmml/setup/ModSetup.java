package net.sistr.lmml.setup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.network.Networking;
import net.sistr.lmml.util.loader.LMFileLoader;
import net.sistr.lmml.util.manager.ModelManager;

@Mod.EventBusSubscriber(modid = LittleMaidModelLoader.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static final ItemGroup ITEM_GROUP = new ItemGroup(LittleMaidModelLoader.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.CAKE);
        }
    };

    public static void init(final FMLCommonSetupEvent event) {

        Networking.registerMessages();

        //リトルメイドファイルローダー
        LMFileLoader.instance.load();

        //マルチモデルセットアップ
        ModelManager.instance.createLittleMaidModels();

        //サウンドパックセットアップ
        //SoundManager.instance.createSounds();

    }

}
