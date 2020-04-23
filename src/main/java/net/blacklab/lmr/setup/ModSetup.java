package net.blacklab.lmr.setup;

import net.blacklab.lmr.ForgeEventHandlers;
import net.blacklab.lmr.LittleMaidReengaged;
import net.blacklab.lmr.config.LMRConfig;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = LittleMaidReengaged.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static final ItemGroup ITEM_GROUP = new ItemGroup("lmreengaged") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.CAKE);
        }
    };

    public static void init(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(ForgeEventHandlers.class);

        if (LMRConfig.CAN_SPAWN_LM.get()) {

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

                    //Biomeタイプが一致した場合にスポーン設定を行う
                    for (BiomeDictionary.Type biomeType : spawnBiomeList) {
                        if (BiomeDictionary.hasType(biome, biomeType)) {
                            isSpawn = true;
                            break;
                        }
                    }

                //スポーン対象の場合はスポーン設定
                if (isSpawn) {
                    //todo AccessTransformerを使うべきか？
                    //biome.addSpawn(EntityClassification.CREATURE, new Biome.SpawnListEntry(Registration.LITTLE_MAID_MOB.get(),
                    //        LMRConfig.SPAWN_WEIGHT_LM.get(), LMRConfig.SPAWN_MIN_GROUP_SIZE_LM.get(), LMRConfig.SPAWN_MAX_GROUP_SIZE_LM.get()));

                }
            }

        }
    }

}
