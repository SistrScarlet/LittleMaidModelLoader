package net.sistr.lmml.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blacklab.lmr.entity.maidmodel.*;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//ココ->Solo->ModelBase
//LivingRendererの処理を流用
//ただし互換性を維持するために、Soloのrenderにて、古いバージョンのrenderにすり替えている
//足りない引数はメンバ変数への直接参照で補っている
@OnlyIn(Dist.CLIENT)
public class MultiModelRenderer<T extends LivingEntity & IHasMultiModel> extends LivingRenderer<T, ModelBaseSolo<T>> {

    //メイド用モデル
    public final ModelBaseSolo<T> modelMain;
    //メイド防具用モデル
    public final ModelBaseDuo<T> modelFATT;
    //メイドモデル用パラメータ管理クラス
    IModelCaps caps;

    public MultiModelRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new ModelBaseSolo<>(), 0.5F);
        //アーマー描画用モデル初期化
        modelFATT = new ModelBaseDuo<>(this);
        modelFATT.isModelAlphablend = true;/*LMRConfig.cfg_isModelAlphaBlend*/;
        modelFATT.isRendering = true;

        //メイド本体描画用モデル初期化
        modelMain = new ModelBaseSolo<>();
        modelMain.isModelAlphablend = true;/*LMRConfig.cfg_isModelAlphaBlend*/;
        modelMain.capsLink = modelFATT;
        entityModel = modelMain;

        this.addLayer(new MMArmorLayer<>(this));
        this.addLayer(new MMHeldItemLayer<>(this));
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks,
                       MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (caps == null) {
            caps = new EntityCaps(entityIn);
        }
        modelMain.entity = entityIn;
        modelMain.entityYaw = entityYaw;
        modelMain.partialTicks = partialTicks;
        modelMain.buffer = bufferIn;
        setModelValues(entityIn, entityIn.getPosX(), entityIn.getPosY(), entityIn.getPosZ(), entityYaw, partialTicks, caps);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    protected boolean canRenderName(T entity) {
        return false;
    }

    public void setModelValues(T entity, double x, double y, double z,
                               float yaw, float partialTicks, IModelCaps caps) {
        ModelMultiBase[] model  = entity.getMultiModels();
        modelMain.model = model[0];
        modelFATT.modelInner = model[1];
        modelFATT.modelOuter = model[2];
        modelMain.textures = entity.getTextures(0);
        modelFATT.textureInner = entity.getTextures(1);
        modelFATT.textureOuter = entity.getTextures(2);
        modelFATT.textureInnerLight = entity.getTextures(3);
        modelFATT.textureOuterLight = entity.getTextures(4);
        modelFATT.textureLightColor = (float[]) modelFATT.getCapsValue(IModelCaps.caps_textureLightColor, caps);

        modelMain.setEntityCaps(caps);
        modelFATT.setEntityCaps(caps);
        modelMain.setRender(this);
        modelFATT.setRender(this);
        modelMain.showAllParts();
        modelFATT.showAllParts();
        modelMain.isAlphablend = true;
        modelFATT.isAlphablend = true;
        modelMain.renderCount = 0;
        modelFATT.renderCount = 0;
        modelMain.lighting = modelFATT.lighting = (int) entity.getBrightness();
        modelMain.setArmorRendering(!entity.isInvisible());

        modelMain.setCapsValue(IModelCaps.caps_heldItemLeft, 0);
        modelMain.setCapsValue(IModelCaps.caps_heldItemRight, 0);
        modelMain.setCapsValue(IModelCaps.caps_isRiding, entity.isBeingRidden());
        modelMain.setCapsValue(IModelCaps.caps_isSneak, entity.isSneaking());
        modelMain.setCapsValue(IModelCaps.caps_aimedBow, false);
        modelMain.setCapsValue(IModelCaps.caps_isWait, false);
        modelMain.setCapsValue(IModelCaps.caps_isChild, entity.isChild());
        modelMain.setCapsValue(IModelCaps.caps_entityIdFactor, 0F);
        modelMain.setCapsValue(IModelCaps.caps_ticksExisted, entity.ticksExisted);
        //カスタム設定
        modelMain.setCapsValue(IModelCaps.caps_motionSitting, false);

        float main = entity.swingingHand == Hand.MAIN_HAND ? entity.getSwingProgress(partialTicks) : 0;
        float off = entity.swingingHand == Hand.OFF_HAND ? entity.getSwingProgress(partialTicks) : 0;
        modelMain.setCapsValue(IModelCaps.caps_onGround, main, off);
    }

    @Override
    public ResourceLocation getEntityTexture(T entity) {
        ResourceLocation location = entity.getTextures(0)[0];
        return location != null ? location : new ResourceLocation("");
    }

}
