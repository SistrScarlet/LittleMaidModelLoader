package net.sistr.lmml.setup;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.entity.IHasMultiModel;
import net.sistr.lmml.entity.MultiModelEntity;
import net.sistr.lmml.maidmodel.ModelLittleMaid_Archetype;
import net.sistr.lmml.maidmodel.ModelLittleMaid_Aug;
import net.sistr.lmml.maidmodel.ModelLittleMaid_Orign;
import net.sistr.lmml.maidmodel.ModelLittleMaid_SR2;
import net.sistr.lmml.network.Networking;
import net.sistr.lmml.util.ResourceHelper;
import net.sistr.lmml.resource.manager.ModelManager;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = LittleMaidModelLoader.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {
        Networking.registerMessages();

        if (FMLEnvironment.dist.isClient()) {
            loadTexture();
        }

        //モデルを読み込む
        ModelManager manager = LittleMaidModelLoader.getInstance().getModelManager();
        manager.addModel("Default", ModelLittleMaid_Orign.class);
        manager.addModel("SR2", ModelLittleMaid_SR2.class);
        manager.addModel("Aug", ModelLittleMaid_Aug.class);
        manager.addModel("Archetype", ModelLittleMaid_Archetype.class);
        manager.setDefaultModel(manager.getModel("Default", IHasMultiModel.Layer.SKIN)
                .orElseThrow(RuntimeException::new));

        LittleMaidModelLoader.getInstance().getFileLoader().load();
        
        //1.16からの追加メソッド
        GlobalEntityTypeAttributes.put(Registration.MULTI_MODEL_ENTITY.get(), MultiModelEntity.registerAttributes().create());
        GlobalEntityTypeAttributes.put(Registration.DUMMY_MODEL_ENTITY.get(), MultiModelEntity.registerAttributes().create());
    }

    @OnlyIn(Dist.CLIENT)
    public static void loadTexture() {
        //このパスにあるテクスチャすべてを受け取る(リソパ及びModリソースからも抜ける)
        Collection<ResourceLocation> resourceLocations = Minecraft.getInstance().getResourceManager()
                .getAllResourceLocations("textures/entity/littlemaid", t -> true);
        //テクスチャを読み込む
        resourceLocations.forEach(resourcePath -> {
            String path = resourcePath.getPath();
            String textureName = ResourceHelper.getTextureName(path);
            assert textureName != null;//canProcessでチェックしてるのでnullはありえない
            String modelName = ResourceHelper.getModelName(textureName);
            int index = ResourceHelper.getIndex(path);
            if (index != -1) {
                LittleMaidModelLoader.getInstance().getTextureManager()
                        .addTexture(ResourceHelper.getFileName(path), textureName, modelName, index, resourcePath);
            }
        });
    }

}
