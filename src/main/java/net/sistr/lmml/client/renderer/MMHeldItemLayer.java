package net.sistr.lmml.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blacklab.lmr.entity.maidmodel.IHasMultiModel;
import net.blacklab.lmr.entity.maidmodel.ModelBaseSolo;
import net.blacklab.lmr.entity.maidmodel.ModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MMHeldItemLayer<T extends LivingEntity & IHasMultiModel> extends LayerRenderer<T, ModelBaseSolo<T>> {
    private final MultiModelRenderer<T> renderer;

    public MMHeldItemLayer(IEntityRenderer<T, ModelBaseSolo<T>> entityRendererIn) {
        super(entityRendererIn);
        renderer = (MultiModelRenderer<T>) entityRendererIn;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flag = entity.getPrimaryHand() == HandSide.RIGHT;
        ItemStack leftStack = flag ? entity.getHeldItemOffhand() : entity.getHeldItemMainhand();
        ItemStack rightStack = flag ? entity.getHeldItemMainhand() : entity.getHeldItemOffhand();
        if (!leftStack.isEmpty() || !rightStack.isEmpty()) {
            matrixStackIn.push();
            if (this.getEntityModel().isChild) {
                matrixStackIn.translate(0.0D, 0.75D, 0.0D);
                matrixStackIn.scale(0.5F, 0.5F, 0.5F);
            }

            this.handRender(entity, rightStack, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStackIn, bufferIn, packedLightIn);
            this.handRender(entity, leftStack, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStackIn, bufferIn, packedLightIn);
            matrixStackIn.pop();
        }
    }

    //todo 位置調整
    private void handRender(LivingEntity entity, ItemStack stack, ItemCameraTransforms.TransformType type, HandSide hand, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light) {
        if (!stack.isEmpty()) {
            matrixStack.push();
            //((IHasArm)this.getEntityModel()).translateHand(hand, matrixStack);
            boolean isLeft = hand == HandSide.LEFT;
            ModelRenderer arm = renderer.modelMain.model.Arms[isLeft ? 1 : 0];
            ModelRenderer.matrixStack = matrixStack;//ちとやり方が汚いか
            arm.postRender(0.0625F);
            if (entity.isSneaking()) {
                matrixStack.translate(0.0F, 0.2F, 0.0F);
            }

            matrixStack.rotate(Vector3f.XP.rotationDegrees(-90.0F));
            matrixStack.rotate(Vector3f.YP.rotationDegrees(180.0F));
            /* 初期モデル構成で
             * x: 手の甲に垂直な方向(-で向かって右に移動)
             * y: 体の面に垂直な方向(-で向かって背面方向に移動)
             * z: 腕に平行な方向(-で向かって手の先方向に移動)
             */
            matrixStack.translate((float)(isLeft ? -1 : 1) / 16.0F, 0.05D, -0.15D);
            Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(entity, stack, type, isLeft, matrixStack, buffer, light);
            matrixStack.pop();
        }
    }

}