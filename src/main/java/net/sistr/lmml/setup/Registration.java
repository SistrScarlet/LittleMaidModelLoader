package net.sistr.lmml.setup;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.sistr.lmml.container.ModelSelectGUIContainer;
import net.sistr.lmml.entity.MultiModelLoadEntity;

import static net.sistr.lmml.LittleMaidModelLoader.MODID;

public class Registration {

    private static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, MODID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, MODID);

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

    public static final RegistryObject<ContainerType<ModelSelectGUIContainer>> MODEL_SELECT_GUI_CONTAINER =
            CONTAINERS.register("model_select_gui_container", () -> IForgeContainerType.create((windowId, inv, data) ->
                    new ModelSelectGUIContainer(windowId)));

}
