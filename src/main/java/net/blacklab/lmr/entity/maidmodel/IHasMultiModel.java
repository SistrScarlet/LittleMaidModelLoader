package net.blacklab.lmr.entity.maidmodel;

import net.minecraft.util.ResourceLocation;

//MultiModelRendererで描画するためのインターフェース
//IModelEntityとほぼ同様
//ここに置くメソッドはgetMultiModels/getTexturesだけで良いのだが、同期処理の時に面倒だったのでこのようになった
public interface IHasMultiModel {

    void updateTextures();

    void setTextures(int index, ResourceLocation[] names);

    ResourceLocation[] getTextures(int index);

    void setMultiModels(int index, ModelMultiBase models);

    //[0]:本体 [1]:防具(内部) [2]:防具(外部)
    ModelMultiBase[] getMultiModels();

    void setColor(byte color);

    byte getColor();

    void setContract(boolean isContract);

    boolean getContract();

    void setTextureBox(int index, TextureBox textureBox);

    TextureBox[] getTextureBox();

}
