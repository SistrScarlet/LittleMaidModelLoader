package net.sistr.lmml.setup;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.sistr.lmml.client.ModelSelectScreen;
import net.sistr.lmml.entity.MultiModelLoadEntity;

import static net.sistr.lmml.LittleMaidModelLoader.MODID;

public class Registration {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MODID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);

    public static void init() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<EntityType<MultiModelLoadEntity>> MULTI_MODEL_LOAD_ENTITY = ENTITIES.register("multi_model_load_entity", () ->
            EntityType.Builder.create((EntityType.IFactory<MultiModelLoadEntity>) MultiModelLoadEntity::new, EntityClassification.MISC)
            .size(0.8F, 1.2F)
            .setShouldReceiveVelocityUpdates(false)
            .build("multi_model_load_entity"));
    public static final RegistryObject<EntityType<ModelSelectScreen.ModelSelectorDummyEntity>> MODEL_SELECTOR_DUMMY_ENTITY = ENTITIES.register("model_selector_dummy_entity", () ->
            EntityType.Builder.create((EntityType.IFactory<ModelSelectScreen.ModelSelectorDummyEntity>) ModelSelectScreen.ModelSelectorDummyEntity::new, EntityClassification.MISC)
                    .size(0.8F, 1.2F)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("model_selector_dummy_entity"));

}
