package net.sistr.lmml.setup;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.client.renderer.MultiModelRenderer;

@Mod.EventBusSubscriber(modid = LittleMaidModelLoader.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(final FMLCommonSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registration.MULTI_MODEL_LOAD_ENTITY.get(), MultiModelRenderer::new);
    }
}
