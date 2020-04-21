package net.blacklab.lmr.setup;

import net.blacklab.lmc.common.entity.LMEntityItemAntiDamage;
import net.blacklab.lmr.entity.littlemaid.EntityLittleMaid;
import net.blacklab.lmr.item.ItemMaidPorter;
import net.blacklab.lmr.item.ItemMaidSpawnEgg;
import net.blacklab.lmr.item.ItemTriggerRegisterKey;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static net.blacklab.lmr.LittleMaidReengaged.DOMAIN;

public class Registration {

    private static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, DOMAIN);
    private static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, DOMAIN);

    public static void init() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<ItemMaidSpawnEgg> SPAWN_LITTLEMAID_EGG = ITEMS.register("spawn_littlemaid_egg", ItemMaidSpawnEgg::new);
    public static final RegistryObject<ItemTriggerRegisterKey> REGISTERKEY = ITEMS.register("registerkey", ItemTriggerRegisterKey::new);
    public static final RegistryObject<ItemMaidPorter> MAIDPORTER = ITEMS.register("maidporter", ItemMaidPorter::new);
    public static final RegistryObject<Item> MAID_SOUVENIR = ITEMS.register("maid_souvenir", ItemMaidSpawnEgg::new);
    public static final RegistryObject<Item> MAID_CARRY = ITEMS.register("maid_carry", ItemMaidSpawnEgg::new);
    public static final RegistryObject<Item> MAID_SUGAR = ITEMS.register("maid_sugar", ItemMaidSpawnEgg::new);
    public static final RegistryObject<Item> PLAYER_MAID_BOOK = ITEMS.register("player_maid_book", ItemMaidSpawnEgg::new);

    public static final RegistryObject<EntityType<EntityLittleMaid>> LITTLE_MAID_MOB = ENTITIES.register("littlemaid", () ->
            EntityType.Builder.create((EntityType.IFactory<EntityLittleMaid>)(type, world) -> new EntityLittleMaid(world), EntityClassification.CREATURE)
                    .size(1F, 1F)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("littlemaid"));
    public static final RegistryObject<EntityType<LMEntityItemAntiDamage>> ANTI_DAMAGE_ITEM_ENTITY = ENTITIES.register("entityitem_antidamage", () ->
            EntityType.Builder.create((EntityType.IFactory<LMEntityItemAntiDamage>)(type, world) -> new LMEntityItemAntiDamage(world), EntityClassification.CREATURE)
                    .size(1F, 1F)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entityitem_antidamage"));

}
