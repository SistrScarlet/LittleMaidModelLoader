package net.sistr.lmml.entity.compound;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sistr.lmml.maidmodel.IModelCaps;
import net.sistr.lmml.maidmodel.ModelMultiBase;
import net.sistr.lmml.resource.holder.TextureHolder;
import net.sistr.lmml.resource.util.TextureColors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

//MultiModelの描画に必要な情報をゲットし、かつScreenから使用テクスチャをセットするためのインターフェース
public interface IHasMultiModel {

    boolean isAllowChangeTexture(@Nullable Entity changer, TextureHolder textureHolder, Layer layer, Part part);

    void setTextureHolder(TextureHolder textureHolder, Layer layer, Part part);

    TextureHolder getTextureHolder(Layer layer, Part part);

    void setColor(TextureColors color);

    TextureColors getColor();

    void setContract(boolean isContract);

    boolean isContract();

    Optional<ModelMultiBase> getModel(Layer layer, Part part);

    @OnlyIn(Dist.CLIENT)
    Optional<ResourceLocation> getTexture(Layer layer, Part part, boolean isLight);

    @Nonnull
    IModelCaps getCaps();

    @OnlyIn(Dist.CLIENT)
    boolean isArmorVisible(Part part);

    @OnlyIn(Dist.CLIENT)
    boolean isArmorGlint(Part part);

    enum Layer {
        SKIN(0, 0, false),
        INNER(1, 0, true),
        OUTER(2, 1, true);

        //indexは連番、partは種類別(体と防具別)
        private final int index;
        private final int partIndex;
        private final boolean isArmor;

        Layer(int index, int partIndex, boolean isArmor) {
            this.index = index;
            this.partIndex = partIndex;
            this.isArmor = isArmor;
        }

        public int getIndex() {
            return index;
        }

        public int getPartIndex() {
            return partIndex;
        }

        public boolean isArmor() {
            return isArmor;
        }
    }

    enum Part {
        HEAD(3, "head"),
        BODY(2, "body"),
        LEGS(1, "legs"),
        FEET(0, "feet");

        private final int index;
        private final String partName;

        Part(int index, String partName) {
            this.index = index;
            this.partName = partName;
        }

        public static Part getPart(int index) {
            for (Part part : Part.values()) {
                if (part.getIndex() == index) {
                    return part;
                }
            }
            throw new IllegalArgumentException("そんなパーツは存在しない");
        }

        public int getIndex() {
            return index;
        }

        public String getPartName() {
            return partName;
        }
    }

}

