package net.blacklab.lmr;

import net.blacklab.lib.vevent.VEventBus;
import net.blacklab.lmc.common.command.LMCommand;
import net.blacklab.lmc.common.helper.ReflectionHelper;
import net.blacklab.lmr.client.resource.OldZipTexturesWrapper;
import net.blacklab.lmr.client.resource.SoundResourcePack;
import net.blacklab.lmr.config.LMRConfig;
import net.blacklab.lmr.event.EventHookLMRE;
import net.blacklab.lmr.network.GuiHandler;
import net.blacklab.lmr.network.LMRNetwork;
import net.blacklab.lmr.network.ProxyClient;
import net.blacklab.lmr.network.ProxyCommon;
import net.blacklab.lmr.setup.Registration;
import net.blacklab.lmr.util.IFF;
import net.blacklab.lmr.util.helper.CommonHelper;
import net.blacklab.lmr.util.loader.LMFileLoader;
import net.blacklab.lmr.util.manager.ModelManager;
import net.blacklab.lmr.util.manager.PluginManager;
import net.blacklab.lmr.util.manager.SoundManager;
import net.firis.lmt.common.LMTCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.resources.IResourcePack;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod("lmreengaged")
public class LittleMaidReengaged {

    public static final String DOMAIN = "lmreengaged";
    public static final String NAME = "LittleMaidReengaged";
    public static final String VERSION = "0.0.1";

    public static final Logger logger = LogManager.getLogger();

    public static ProxyCommon proxy = DistExecutor.runForDist(() -> ProxyClient::new, () -> ProxyCommon::new);

    /**
     * 開発用デバッグログ
     */
    public static void Debug(String pText, Object... pVals) {
        // デバッグメッセージ
        if (LMRConfig.cfg_PrintDebugMessage) {
            System.out.println(String.format("littleMaidMob-" + pText, pVals));
        }
    }

    /**
     * 開発用デバッグログ
     */
    public static void Debug(boolean isRemote, String format, Object... pVals) {
        Debug("Side=%s; ".concat(format), isRemote, pVals);
    }

    //登録周りは全取り換えになると思うので一旦詰め込んだ
    public LittleMaidReengaged() {

        Registration.init();

        LMRConfig.init(evt.getSuggestedConfigurationFile());

        //リトルメイドファイルローダー
        LMFileLoader.instance.load();

        //マルチモデルセットアップ
        ModelManager.instance.createLittleMaidModels();

        //サウンドパックセットアップ
        SoundManager.instance.createSounds();

        //Pluginクラスをロードする登録処理
        PluginManager.preInitPluginLoad(evt);

        // アイテムスロット更新用のパケット
        LMRNetwork.init(DOMAIN);

        //テスト用preInit
        LMTCore.preInit(evt);

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        MinecraftForge.EVENT_BUS.register(new EventHookLMRE());
        VEventBus.instance.registerListener(new EventHookLMRE());

        //描画イベント登録
        proxy.initClientRendererEventRegister();

        //Plugin初期化処理
        PluginManager.initRegisterPlugin(event);

        if (LMRConfig.cfg_spawnWeight > 0) {

            //メイドさんのスポーンバイオーム
            List<BiomeDictionary.Type> spawnBiomeList = new ArrayList<>();
            spawnBiomeList.add(BiomeDictionary.Type.WET);
            spawnBiomeList.add(BiomeDictionary.Type.DRY);
            spawnBiomeList.add(BiomeDictionary.Type.SAVANNA);
            spawnBiomeList.add(BiomeDictionary.Type.CONIFEROUS);
            spawnBiomeList.add(BiomeDictionary.Type.MUSHROOM);
            spawnBiomeList.add(BiomeDictionary.Type.FOREST);
            spawnBiomeList.add(BiomeDictionary.Type.PLAINS);
            spawnBiomeList.add(BiomeDictionary.Type.SANDY);
            spawnBiomeList.add(BiomeDictionary.Type.BEACH);

            //バイオーム単位でスポーン設定を行う
            for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
                if (biome == null) continue;

                boolean isSpawn = false;

                //デフォルトスポーン設定
                if (LMRConfig.cfg_spawn_default_enable) {
                    //Biomeタイプが一致した場合にスポーン設定を行う
                    for (BiomeDictionary.Type biomeType : spawnBiomeList) {
                        if (BiomeDictionary.hasType(biome, biomeType)) {
                            isSpawn = true;
                            break;
                        }
                    }
                }

                //カスタムスポーン設定
                if (!isSpawn) {
                    for (String spawnBiome : LMRConfig.cfg_spawn_biomes) {

                        String biomeName = ReflectionHelper.getField(Biome.class, biome, "biomeName", "field_76791_y");

                        //バイオーム名 or バイオームID
                        if (spawnBiome.equals(biomeName)
                                || spawnBiome.equals(biome.getRegistryName().toString())) {
                            isSpawn = true;
                            break;
                        }
                    }
                }

                //スポーン対象の場合はスポーン設定
                if (isSpawn) {
					/*仮停止
					EntityRegistry.addSpawn(EntityLittleMaid.class,
							LMRConfig.cfg_spawnWeight,
							LMRConfig.cfg_minGroupSize,
							LMRConfig.cfg_maxGroupSize,
							EnumCreatureType.CREATURE, biome);
					Debug("Registering maids to spawn in " + biome);
					 */
                }
            }

        }

        // IFFのロード
        IFF.loadIFFs();

        //テスト用モジュール登録
        LMTCore.registerItems(event);

        // Register model and renderer
        proxy.rendererRegister();

        // メイドの土産
        ModelLoader.setCustomModelResourceLocation(LMItems.MAID_SOUVENIR, 0,
                new ModelResourceLocation(LMItems.MAID_SOUVENIR.getRegistryName(), "inventory"));

        // メイドキャリー
        ModelLoader.setCustomModelResourceLocation(LMItems.MAID_CARRY, 0,
                new ModelResourceLocation(LMItems.MAID_CARRY.getRegistryName(), "inventory"));

        // メイドシュガー
        ModelLoader.setCustomModelResourceLocation(LMItems.MAID_SUGAR, 0,
                new ModelResourceLocation(LMItems.MAID_SUGAR.getRegistryName(), "inventory"));

        //リトルメイドテスト用モジュール
        LMTCore.registerModels(event);

        LMTCore.registerEntities(entityId);

        //メイドさんコマンドの追加
        event.registerServerCommand(new LMCommand());

        if (CommonHelper.isClient) {
            List<IResourcePack> defaultResourcePacks = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getInstance(), "field_110449" + "_ao");

            defaultResourcePacks.add(new SoundResourcePack());
            defaultResourcePacks.add(new OldZipTexturesWrapper());
        }
    }

}
