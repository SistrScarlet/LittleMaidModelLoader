package net.blacklab.lmr.entity.maidmodel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.sistr.lmml.entity.MultiModelLoadEntity;

import java.util.Map;

//新Rendererと旧Modelを繋ぐ
//EntityModel継承であるため、ここまではバニラのLivingRenderer内で処理される
public class ModelBaseSolo<T extends LivingEntity> extends ModelBaseNihil<T> implements IModelBaseMMM {
    public ModelMultiBase model;

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (model == null) {
            isAlphablend = false;
            return;
        }

        ModelRenderer.matrixStack = matrixStackIn;
        ModelRenderer.buffer = bufferIn;
        ModelRenderer.packedLight = packedLightIn;
        ModelRenderer.packedOverlay = packedOverlayIn;
        ModelRenderer.red = red;
        ModelRenderer.green = blue;
        ModelRenderer.blue = green;
        ModelRenderer.alpha = alpha;

        float f = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        float f1 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
        float headYaw = f1 - f;

        boolean shouldSit = this.entity.isPassenger() && (this.entity.getRidingEntity() != null && this.entity.getRidingEntity().shouldRiderSit());
        if (shouldSit && entity.getRidingEntity() instanceof LivingEntity) {
            LivingEntity entitylivingbase = (LivingEntity) entity.getRidingEntity();
            f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
            headYaw = f1 - f;
            float f3 = MathHelper.wrapDegrees(headYaw);

            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            f = f1 - f3;

            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }

            headYaw = f1 - f;
        }

        float headPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        float ageInTicks = (float) entity.ticksExisted + partialTicks;
        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;

        if (!entity.isBeingRidden()) {
            limbSwingAmount = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
            limbSwing = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

            if (entity.isChild()) {
                limbSwing *= 3.0F;
            }

            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
            headYaw = f1 - f; // Forge: Fix MC-1207
        }

        model.render(entityCaps, limbSwing, limbSwingAmount,
                ageInTicks, headYaw, headPitch, 0.0625F, true);

        super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (model != null) {
            model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 1, entityCaps);
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
    public RenderType getRenderTypeMM(ResourceLocation resourcelocation) {
        return RenderType.getEntityCutoutNoCull(resourcelocation);
    }

    public ResourceLocation getEntityTexture(T entity) {
        return ((MultiModelLoadEntity)entity).texture.getTextureName(((MultiModelLoadEntity) entity).color);
    }

    /**
     * Returns a rotation angle that is inbetween two other rotation angles. par1 and par2 are the angles between which
     * to interpolate, par3 is probably a float between 0.0 and 1.0 that tells us where "between" the two angles we are.
     * Example: par1 = 30, par2 = 50, par3 = 0.5, then return = 40
     */
    protected float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f = yawOffset - prevYawOffset;

        while (f < -180.0F) {
            f += 360.0F;
        }

        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return prevYawOffset + partialTicks * f;
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
