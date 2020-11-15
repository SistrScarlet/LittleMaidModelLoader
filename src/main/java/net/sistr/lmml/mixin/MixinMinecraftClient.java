package net.sistr.lmml.mixin;

import net.minecraft.client.GameConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.config.LMMLConfig;
import net.sistr.lmml.resource.manager.LMTextureManager;
import net.sistr.lmml.resource.util.ResourceHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@OnlyIn(Dist.CLIENT)
@Mixin(Minecraft.class)
public class MixinMinecraftClient {

    @Inject(at = @At("RETURN"), method = "<init>")
    public void onInit(GameConfiguration args, CallbackInfo ci) {
        loadTexture();
    }

    private static void loadTexture() {
        //このパスにあるテクスチャすべてを受け取る(リソパ及びModリソースからも抜ける)
        Collection<ResourceLocation> resourceLocations = Minecraft.getInstance().getResourceManager()
                .getAllResourceLocations("textures/entity/littlemaid", s -> true);
        //テクスチャを読み込む
        resourceLocations.forEach(resourcePath -> {
            String path = resourcePath.getPath();
            ResourceHelper.getParentFolderName(path, true).ifPresent(textureName -> {
                String modelName = ResourceHelper.getModelName(textureName);
                int index = ResourceHelper.getIndex(path);
                if (index != -1) {
                    LMTextureManager.INSTANCE
                            .addTexture(ResourceHelper.getFileName(path, true), textureName, modelName, index, resourcePath);
                }
            });
            if (LMMLConfig.isDebugMode()) {
                LittleMaidModelLoader.LOGGER.debug(resourcePath);
            }
        });
    }

}
