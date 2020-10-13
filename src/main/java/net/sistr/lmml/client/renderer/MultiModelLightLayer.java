package net.sistr.lmml.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.profiler.IProfiler;
import net.sistr.lmml.entity.IHasMultiModel;
import net.sistr.lmml.maidmodel.IModelCaps;
import net.sistr.lmml.maidmodel.ModelRenderer;

//スキンの発光レイヤー、防具の発光レイヤーは防具でやってる
public class MultiModelLightLayer<T extends LivingEntity & IHasMultiModel> extends LayerRenderer<T, MultiModel<T>> {

    public MultiModelLightLayer(IEntityRenderer<T, MultiModel<T>> entityRendererIn) {
        super(entityRendererIn);
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity,
                       float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        IProfiler profiler = Minecraft.getInstance().getProfiler();
        profiler.startSection("lmml:mm_eye_layer");
        renderLightLayer(matrixStackIn, bufferIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks,
                netHeadYaw, headPitch, entity.getCaps());
        profiler.endSection();
    }

    //クモの目と同じRenderTypeを使いたいがなんか真っ白になるのでやめた
    private void renderLightLayer(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, T entity,
                                  float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                  float netHeadYaw, float headPitch, IModelCaps caps) {
        entity.getTexture(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD, true).ifPresent(resourceLocation ->
                entity.getModel(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD).ifPresent(model -> {
                    IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityTranslucent(resourceLocation));
                    ModelRenderer.setParam(matrixStackIn, builder, 0xF00000,
                            OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
                    float renderScale = 0.0625F;
                    model.setLivingAnimations(caps, limbSwing, limbSwingAmount, partialTicks);
                    model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, renderScale, caps);
                    model.render(caps, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, renderScale, true);
                }));
    }

}
