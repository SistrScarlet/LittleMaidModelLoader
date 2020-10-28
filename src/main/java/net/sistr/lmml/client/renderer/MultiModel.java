package net.sistr.lmml.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.sistr.lmml.entity.compound.IHasMultiModel;
import net.sistr.lmml.maidmodel.ModelRenderer;

public class MultiModel<T extends LivingEntity & IHasMultiModel> extends EntityModel<T> {
    private T entity;
    private float limbSwing;
    private float limbSwingAmount;
    private float ageInTicks;
    private float netHeadYaw;
    private float headPitch;

    public MultiModel() {
        super(RenderType::getEntityTranslucent);
    }

    @Override
    public void setLivingAnimations(T entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
        entityIn.getModel(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD).ifPresent(model ->
                model.setLivingAnimations(entityIn.getCaps(), limbSwing, limbSwingAmount, partialTick));
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.entity = entityIn;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        entityIn.getModel(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD).ifPresent(model ->
                model.setRotationAngles(limbSwing, limbSwingAmount,
                        ageInTicks, netHeadYaw, headPitch, 0.0625F, entityIn.getCaps()));
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (this.entity == null) {
            return;
        }
        this.entity.getModel(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD).ifPresent(model -> {
            ModelRenderer.setParam(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            model.render(entity.getCaps(), limbSwing, limbSwingAmount,
                    ageInTicks, netHeadYaw, headPitch, 0.0625F, true);
        });
        this.entity = null;
    }

}