package net.blacklab.lmr.entity.maidmodel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

/**
 * アーマーの二重描画用クラス。
 * 必ずInner側にはモデルを設定すること。
 * 通常のRendererで描画するためのクラスなので、Renderをちゃんと記述するならいらないクラスです。
 */
public class ModelBaseDuo<T extends LivingEntity> extends ModelBaseNihil<T> implements IModelBaseMMM {

    public LivingRenderer<T, ModelBaseSolo<T>> renderer;

    public ModelMultiBase modelOuter;
    public ModelMultiBase modelInner;
    /**
     * 部位毎のアーマーテクスチャの指定。
     * 外側。
     */
    public ResourceLocation[] textureOuter;
    /**
     * 部位毎のアーマーテクスチャの指定。
     * 内側。
     */
    public ResourceLocation[] textureInner;
    /**
     * 部位毎のアーマーテクスチャの指定。
     * 外側・発光。
     */
    public ResourceLocation[] textureOuterLight;
    /**
     * 部位毎のアーマーテクスチャの指定。
     * 内側・発光。
     */
    public ResourceLocation[] textureInnerLight;
    /**
     * 描画されるアーマーの部位。
     * shouldRenderPassとかで指定する。
     */
    public int renderParts;

    public float[] textureLightColor;

    public ModelBaseDuo(LivingRenderer<T, ModelBaseSolo<T>> renderer) {
        this.renderer = renderer;
        renderParts = 0;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        //発動しない。MMArmorLayerからどうぞ
        //こっちに処理書いてLayerから読み出すでもいいっちゃいいんだけどね
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (modelInner != null) {
            modelInner.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 0.0625F, entityCaps);
        }
        if (modelOuter != null) {
            modelOuter.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 0.0625F, entityCaps);
        }
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public void setLivingAnimations(T entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
        if (modelInner != null) {
            modelInner.setLivingAnimations(entityCaps, limbSwing, limbSwingAmount, partialTickTime);
        }
        if (modelOuter != null) {
            modelOuter.setLivingAnimations(entityCaps, limbSwing, limbSwingAmount, partialTickTime);
        }
        isAlphablend = true;
        super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
    }

    // IModelMMM追加分

    @Override
    public void renderItems(LivingEntity pEntity, EntityRenderer<?> pRender) {
        if (modelInner != null) {
            modelInner.renderItems(entityCaps);
        }
    }

    @Override
    public void showArmorParts(int pParts) {
        if (modelInner != null) {
            modelInner.showArmorParts(pParts, 0);
        }
        if (modelOuter != null) {
            modelOuter.showArmorParts(pParts, 1);
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
        if (modelInner != null) {
            modelInner.render = pRender;
        }
        if (modelOuter != null) {
            modelOuter.render = pRender;
        }
    }

    @Override
    public void setArmorRendering(boolean pFlag) {
        isRendering = pFlag;
    }

    // IModelCaps追加分

    @Override
    public Map<String, Integer> getModelCaps() {
        return modelInner == null ? null : modelInner.getModelCaps();
    }

    @Override
    public Object getCapsValue(int pIndex, Object... pArg) {
        return modelInner == null ? null : modelInner.getCapsValue(pIndex, pArg);
    }

    @Override
    public boolean setCapsValue(int pIndex, Object... pArg) {
        if (capsLink != null) {
            capsLink.setCapsValue(pIndex, pArg);
        }
        if (modelOuter != null) {
            modelOuter.setCapsValue(pIndex, pArg);
        }
        if (modelInner != null) {
            return modelInner.setCapsValue(pIndex, pArg);
        }
        return false;
    }

    @Override
    public void showAllParts() {
        if (modelInner != null) {
            modelInner.showAllParts();
        }
        if (modelOuter != null) {
            modelOuter.showAllParts();
        }
    }

    @Deprecated
    public Object getTextureOffset(String par1Str) {
        return modelInner == null ? null : modelInner.getTextureOffset(par1Str);
    }

}
