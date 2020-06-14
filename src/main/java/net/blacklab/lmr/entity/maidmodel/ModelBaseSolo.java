package net.blacklab.lmr.entity.maidmodel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.sistr.lmml.client.CompatEntityRenderMath;

import java.util.Map;

//新Rendererと旧Modelを繋ぐ
//EntityModel継承であるため、ここまではバニラのLivingRenderer内で処理される
public class ModelBaseSolo<T extends LivingEntity> extends ModelBaseNihil<T> implements IModelBaseMMM {

    public ModelMultiBase model;
    public ResourceLocation[] textures;

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (model == null) {
            isAlphablend = false;
            return;
        }

        float[] limb = CompatEntityRenderMath.getLimbSwing(entity, partialTicks);
        float[] yawPitch = CompatEntityRenderMath.getYawPitch(entity, partialTicks);

        float limbSwing = limb[0];
        float limbSwingAmount = limb[1];
        float ageInTicks = CompatEntityRenderMath.getAgeInTicks(entity, partialTicks);
        float headYaw = yawPitch[0];
        float headPitch = yawPitch[1];

        // 通常
        //todo アルファブレンド設定が要れば
        ModelRenderer.setParam(matrixStackIn,
                buffer.getBuffer(isAlphablend /*&& LMRConfig.cfg_isModelAlphaBlend*/ ?
                        RenderType.getEntityTranslucent(textures[0]) :
                        RenderType.getEntityCutoutNoCull(textures[0])),
                packedLightIn, packedOverlayIn, red, green, blue, alpha);
        model.render(entityCaps, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, 0.0625F, isRendering);

        isAlphablend = false;

        if (textures.length > 1 && textures[1] != null && renderCount == 0) {
            ModelRenderer.setParam(matrixStackIn,
                    buffer.getBuffer(RenderType.getEntityTranslucent(textures[1])),
                    255, packedOverlayIn, red, green, blue, alpha);
            model.render(entityCaps, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, 0.0625F, isRendering);
        }

        renderCount++;
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (model != null) {
            model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 0.0625F, entityCaps);
        }
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void setLivingAnimations(T entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
        if (model != null) {
            try {
                model.setLivingAnimations(entityCaps, limbSwing, limbSwingAmount, partialTickTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isAlphablend = true;
    }

    @Override
    public void renderItems(LivingEntity pEntity, EntityRenderer<?> pRender) {
        if (model != null) {
            model.renderItems(entityCaps);
        }
    }

    @Override
    public void showArmorParts(int pParts) {
        if (model != null) {
            model.showArmorParts(pParts, 0);
        }
    }

    /**
     * Renderer辺でこの変数を設定する。
     * 設定値はIModelCapsを継承したEntitiyとかを想定。
     */
    @Override
    public void setEntityCaps(IModelCaps pEntityCaps) {
        entityCaps = pEntityCaps;
        if (capsLink != null) {
            capsLink.setEntityCaps(pEntityCaps);
        }
    }

    @Override
    public void setRender(EntityRenderer<?> pRender) {
        if (model != null) {
            model.render = pRender;
        }
    }

    @Override
    public void setArmorRendering(boolean pFlag) {
        isRendering = pFlag;
    }

    // IModelCaps追加分

    @Override
    public Map<String, Integer> getModelCaps() {
        return model == null ? null : model.getModelCaps();
    }

    @Override
    public Object getCapsValue(int pIndex, Object... pArg) {
        return model == null ? null : model.getCapsValue(pIndex, pArg);
    }

    @Override
    public boolean setCapsValue(int pIndex, Object... pArg) {
        if (capsLink != null) {
            capsLink.setCapsValue(pIndex, pArg);
        }
        if (model != null) {
            model.setCapsValue(pIndex, pArg);
        }
        return false;
    }

    @Override
    public void showAllParts() {
        if (model != null) {
            model.showAllParts();
        }
    }


}
