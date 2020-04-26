package net.sistr.lmml.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sistr.lmml.client.maidmodel.EntityCaps;
import net.sistr.lmml.client.maidmodel.IModelCaps;
import net.sistr.lmml.client.maidmodel.ModelBaseSolo;
import net.sistr.lmml.client.maidmodel.ModelLittleMaid_Aug;
import net.sistr.lmml.entity.MultiModelLoadEntity;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class MultiModelRenderer extends LivingRenderer<MultiModelLoadEntity, ModelBaseSolo<MultiModelLoadEntity>> {
    IModelCaps caps;

    public MultiModelRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new ModelBaseSolo<>(), 0.5F);
        entityModel.model = new ModelLittleMaid_Aug();
    }

    @Override
    public void render(MultiModelLoadEntity entityIn, float entityYaw, float partialTicks,
                       MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (caps == null) {
            caps = new EntityCaps(entityIn);
        }
        entityModel.entity = entityIn;
        entityModel.entityYaw = entityYaw;
        entityModel.partialTicks = partialTicks;
        entityModel.buffer = bufferIn;
        setModelValues(entityIn, entityIn.getPosX(), entityIn.getPosY(), entityIn.getPosZ(), entityYaw, partialTicks, caps);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    public void setModelValues(MultiModelLoadEntity entity, double x, double y, double z,
                               float yaw, float partialTicks, IModelCaps caps) {
        entityModel.setEntityCaps(caps);
        //modelFATT.setEntityCaps(caps);
        entityModel.setRender(this);
        //modelFATT.setRender(this);
        entityModel.showAllParts();
        //modelFATT.showAllParts();
        entityModel.isAlphablend = true;
        //modelFATT.isAlphablend = true;
        entityModel.renderCount = 0;
        //modelFATT.renderCount = 0;
        entityModel.lighting = /*modelFATT.lighting = */(int)entity.getBrightness();

        entityModel.setCapsValue(IModelCaps.caps_heldItemLeft, 0);
        entityModel.setCapsValue(IModelCaps.caps_heldItemRight, 0);
//		entityModel.setCapsValue(IModelCaps.caps_onGround, getSwingProgress(entity, partialTicks));
        entityModel.setCapsValue(IModelCaps.caps_isRiding, entity.isBeingRidden());
        entityModel.setCapsValue(IModelCaps.caps_isSneak, entity.isShiftKeyDown());
        entityModel.setCapsValue(IModelCaps.caps_aimedBow, false);
        entityModel.setCapsValue(IModelCaps.caps_isWait, false);
        entityModel.setCapsValue(IModelCaps.caps_isChild, entity.isChild());
        entityModel.setCapsValue(IModelCaps.caps_entityIdFactor, 0F);
        entityModel.setCapsValue(IModelCaps.caps_ticksExisted, entity.ticksExisted);
        //カスタム設定
        entityModel.setCapsValue(IModelCaps.caps_motionSitting, false);
    }

    @Nullable
    protected RenderType func_230042_a_(MultiModelLoadEntity entity, boolean canSee, boolean translucent) {
        ResourceLocation resourcelocation = this.getEntityTexture(entity);
        if (translucent) {
            return RenderType.getEntityTranslucent(resourcelocation);
        } else if (canSee) {
            return this.entityModel.getRenderTypeMM(resourcelocation);
        } else {
            return entity.isGlowing() ? RenderType.getOutline(resourcelocation) : null;
        }
    }

    @Override
    public ResourceLocation getEntityTexture(MultiModelLoadEntity entity) {
        return entityModel.getEntityTexture(entity);
    }

}
