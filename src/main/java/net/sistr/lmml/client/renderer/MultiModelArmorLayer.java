package net.sistr.lmml.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.profiler.IProfiler;
import net.sistr.lmml.entity.compound.IHasMultiModel;
import net.sistr.lmml.maidmodel.IModelCaps;
import net.sistr.lmml.maidmodel.ModelRenderer;

//todo 重すぎる
public class MultiModelArmorLayer<T extends LivingEntity & IHasMultiModel> extends LayerRenderer<T, MultiModel<T>> {

    public MultiModelArmorLayer(IEntityRenderer<T, MultiModel<T>> entityRendererIn) {
        super(entityRendererIn);
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity,
                       float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        IProfiler profiler = Minecraft.getInstance().getProfiler();
        profiler.startSection("lmml:mm_armor_layer");
        this.renderArmorPart(matrixStackIn, bufferIn, packedLightIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks,
                netHeadYaw, headPitch, IHasMultiModel.Part.HEAD);
        this.renderArmorPart(matrixStackIn, bufferIn, packedLightIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks,
                netHeadYaw, headPitch, IHasMultiModel.Part.BODY);
        this.renderArmorPart(matrixStackIn, bufferIn, packedLightIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks,
                netHeadYaw, headPitch, IHasMultiModel.Part.LEGS);
        this.renderArmorPart(matrixStackIn, bufferIn, packedLightIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks,
                netHeadYaw, headPitch, IHasMultiModel.Part.FEET);
        profiler.endSection();
    }

    private void renderArmorPart(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity,
                                 float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                 float netHeadYaw, float headPitch, IHasMultiModel.Part part) {
        if (!entity.isArmorVisible(part)) {
            return;
        }

        boolean glint = entity.isArmorGlint(part);

        IModelCaps caps = entity.getCaps();

        renderArmorLayer(matrixStackIn, bufferIn, packedLightIn, entity,
                limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch,
                part, IHasMultiModel.Layer.INNER, false, caps, glint);
        renderArmorLayer(matrixStackIn, bufferIn, packedLightIn, entity,
                limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch,
                part, IHasMultiModel.Layer.INNER, true, caps, glint);
        renderArmorLayer(matrixStackIn, bufferIn, packedLightIn, entity,
                limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch,
                part, IHasMultiModel.Layer.OUTER, false, caps, glint);
        renderArmorLayer(matrixStackIn, bufferIn, packedLightIn, entity,
                limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch,
                part, IHasMultiModel.Layer.OUTER, true, caps, glint);

    }

    private void renderArmorLayer(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity,
                                  float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                  float netHeadYaw, float headPitch,
                                  IHasMultiModel.Part part, IHasMultiModel.Layer layer, boolean isLight, IModelCaps caps,
                                  boolean glint) {
        entity.getTexture(layer, part, isLight).ifPresent(resourceLocation ->
                entity.getModel(layer, part).ifPresent(model -> {
                    model.showArmorParts(part.getIndex(), layer.getPartIndex());
                    RenderType type = RenderType.getEntityTranslucent(resourceLocation);
                    IVertexBuilder builder = ItemRenderer.getBuffer(bufferIn, type, false, glint);
                    int light = isLight ? 0xF00000 : packedLightIn;
                    ModelRenderer.setParam(matrixStackIn, builder, light,
                            OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
                    float renderScale = 0.0625F;
                    model.setLivingAnimations(caps, limbSwing, limbSwingAmount, partialTicks);
                    model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, renderScale, caps);
                    model.render(caps, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, renderScale, true);
                }));
    }

}
