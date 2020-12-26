package net.sistr.lmml.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.entity.compound.IHasMultiModel;
import net.sistr.lmml.maidmodel.ModelMultiBase;

import javax.annotation.Nonnull;

import static net.sistr.lmml.maidmodel.IModelCaps.*;

//そのまま使ってもいいし継承して使ってもいい
//別な奴を継承しながら使いたいなら移譲でどうにかするか自作してね
public class MultiModelRenderer<T extends LivingEntity & IHasMultiModel> extends LivingRenderer<T, MultiModel<T>> {
    private static final ResourceLocation NULL_TEXTURE = new ResourceLocation(LittleMaidModelLoader.MODID, "null");

    public MultiModelRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new MultiModel<>(), 0.5F);
        this.addLayer(new MultiModelArmorLayer<>(this));
        this.addLayer(new MultiModelHeldItemLayer<>(this));
        this.addLayer(new MultiModelLightLayer<>(this));
    }

    //MobRendererから引っ張ってきた
    protected boolean canRenderName(T entity) {
        return super.canRenderName(entity) && (entity.getAlwaysRenderNameTagForRender()
                || entity.hasCustomName() && entity == this.renderManager.pointedEntity);
    }

    @Override
    protected void preRenderCallback(T entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
        entitylivingbaseIn.getModel(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD).ifPresent(model -> {
            float scale = (float) model.getCapsValue(caps_ScaleFactor);
            matrixStackIn.scale(scale, scale, scale);
        });

    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        IProfiler profiler = Minecraft.getInstance().getProfiler();
        profiler.startSection("lmml:mm");
        entityIn.getModel(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD)
                .ifPresent(model -> syncCaps(entityIn, model, partialTicks));
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            entityIn.getModel(IHasMultiModel.Layer.INNER, part)
                    .ifPresent(model -> syncCaps(entityIn, model, partialTicks));
            entityIn.getModel(IHasMultiModel.Layer.OUTER, part)
                    .ifPresent(model -> syncCaps(entityIn, model, partialTicks));
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        profiler.endSection();
    }

    public void syncCaps(T entity, ModelMultiBase model, float partialTicks) {
        float swingProgress = entity.getSwingProgress(partialTicks);
        float right = 0;
        float left = 0;
        if (entity.swingingHand == Hand.MAIN_HAND) {
            if (entity.getPrimaryHand() == HandSide.RIGHT) {
                right = swingProgress;
            } else {
                left = swingProgress;
            }
        } else {
            if (entity.getPrimaryHand() != HandSide.RIGHT) {
                right = swingProgress;
            } else {
                left = swingProgress;
            }
        }
        model.setCapsValue(caps_onGround, right, left);
        model.setCapsValue(caps_isRiding, entity.isPassenger()
                && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit()));
        model.setCapsValue(caps_isSneak, entity.isSneaking());
        model.setCapsValue(caps_isChild, entity.isChild());
        model.setCapsValue(caps_heldItemLeft, 0F);
        model.setCapsValue(caps_heldItemRight, 0F);
        model.setCapsValue(caps_aimedBow, false);
        model.setCapsValue(caps_entityIdFactor, 0F);
        model.setCapsValue(caps_ticksExisted, entity.ticksExisted);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(T entity) {
        return entity.getTexture(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD, false)
                .orElse(NULL_TEXTURE);
    }

}
