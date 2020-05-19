package net.sistr.lmml.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blacklab.lmr.entity.maidmodel.IHasMultiModel;
import net.blacklab.lmr.entity.maidmodel.ModelBaseDuo;
import net.blacklab.lmr.entity.maidmodel.ModelBaseSolo;
import net.blacklab.lmr.entity.maidmodel.ModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

public class MMArmorLayer<T extends LivingEntity & IHasMultiModel> extends LayerRenderer<T, ModelBaseSolo<T>> {

    private final MultiModelRenderer<T> renderer;
    private final ModelBaseDuo<T> model;

    public MMArmorLayer(IEntityRenderer<T, ModelBaseSolo<T>> entityRendererIn) {
        super(entityRendererIn);
        this.renderer = (MultiModelRenderer<T>) entityRendererIn;
        this.model = this.renderer.modelFATT;
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.renderArmorPart(matrixStackIn, bufferIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.CHEST, packedLightIn);
        this.renderArmorPart(matrixStackIn, bufferIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.LEGS, packedLightIn);
        this.renderArmorPart(matrixStackIn, bufferIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.FEET, packedLightIn);
        this.renderArmorPart(matrixStackIn, bufferIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.HEAD, packedLightIn);

    }

    private void renderArmorPart(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, EquipmentSlotType slot, int packedLightIn) {
        if (entity.getItemStackFromSlot(slot).isEmpty()) {
            return;
        }

        //描画するモデルを選択
        renderer.modelFATT.showArmorParts(slot.getIndex());

        float renderScale = 0.0625F;

        //Inner
        if (model.textureInner != null) {
            ResourceLocation texInner = model.textureInner[slot.getIndex()];
            if (texInner != null) {
                ModelRenderer.setParam(matrixStackIn,
                        bufferIn.getBuffer(RenderType.getEntityTranslucent(texInner)),
                        packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
                model.modelInner.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, renderScale, renderer.caps);
                model.modelInner.setLivingAnimations(renderer.caps, limbSwing, limbSwingAmount, partialTicks);
                model.modelInner.render(renderer.caps, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, renderScale, true);

            }
        }

        //Outer
        if (model.textureOuter != null) {
            ResourceLocation texOuter = model.textureOuter[slot.getIndex()];
            if (texOuter != null) {
                ModelRenderer.setParam(matrixStackIn,
                        bufferIn.getBuffer(RenderType.getEntityTranslucent(texOuter)),
                        packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
                model.modelOuter.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, renderScale, renderer.caps);
                model.modelOuter.setLivingAnimations(renderer.caps, limbSwing, limbSwingAmount, partialTicks);
                model.modelOuter.render(renderer.caps, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, renderScale, true);

            }
        }

        // 発光Inner
        if (model.modelInner != null) {
            ResourceLocation texInnerLight = model.textureInnerLight[slot.getIndex()];
            if (texInnerLight != null) {
                ModelRenderer.setParam(matrixStackIn,
                        bufferIn.getBuffer(RenderType.getEntityTranslucent(texInnerLight)),
                        255, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
                model.modelInner.render(renderer.caps, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, renderScale, true);
            }
        }

        // 発光Outer
        if (model.modelOuter != null) {
            ResourceLocation texOuterLight = model.textureOuterLight[slot.getIndex()];
            if (texOuterLight != null) {
                ModelRenderer.setParam(matrixStackIn,
                        bufferIn.getBuffer(RenderType.getEntityTranslucent(texOuterLight)),
                        255, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
                model.modelOuter.render(renderer.caps, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, renderScale, true);
            }
        }
    }

}
