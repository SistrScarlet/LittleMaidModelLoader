package net.sistr.lmml.setup;

import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.sistr.lmml.ForgeEventHandlers;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.entity.MultiModelLoadEntity;
import net.sistr.lmml.network.Networking;
import net.sistr.lmml.util.loader.LMFileLoader;
import net.sistr.lmml.util.manager.ModelManager;

@Mod.EventBusSubscriber(modid = LittleMaidModelLoader.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    /*public static final ItemGroup ITEM_GROUP = new ItemGroup(LittleMaidModelLoader.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.CAKE);
        }
    };*/

    public static void init(final FMLCommonSetupEvent event) {

        GlobalEntityTypeAttributes.put(Registration.MULTI_MODEL_LOAD_ENTITY.get(), MultiModelLoadEntity.registerAttributes().func_233813_a_());
        GlobalEntityTypeAttributes.put(Registration.MODEL_SELECTOR_DUMMY_ENTITY.get(), MultiModelLoadEntity.registerAttributes().func_233813_a_());

        Networking.registerMessages();

        //リトルメイドファイルローダー
        LMFileLoader.instance.load();

        //マルチモデルセットアップ
        ModelManager.instance.createLittleMaidModels();

        //サウンドパックセットアップ
        //SoundManager.instance.createSounds();

        MinecraftForge.EVENT_BUS.register(ForgeEventHandlers.class);

    }

}
