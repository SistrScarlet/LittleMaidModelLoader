package net.sistr.lmml.entity;

import net.blacklab.lmr.entity.maidmodel.IHasMultiModel;
import net.blacklab.lmr.entity.maidmodel.ModelMultiBase;
import net.blacklab.lmr.entity.maidmodel.TextureBox;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;
import net.sistr.lmml.network.Networking;
import net.sistr.lmml.network.PacketSyncModel;
import net.sistr.lmml.util.manager.ModelManager;

import javax.annotation.Nullable;

//モデル/テクスチャの管理クラス
//ModelConfigCompoundを流用
//TextureBoxはサーバー側で保持するが、それ以外は保持しない
public class DefaultMultiModel implements IHasMultiModel {

    private final TextureBox defaultMainBoxes;
    private final TextureBox defaultArmorBoxes;

    private final ResourceLocation[][] textures = new ResourceLocation[][]{
            //基本、発光
            {null, null},
            //アーマー内：頭、胴、腰、足
            {null, null, null, null},
            //アーマー外：頭、胴、腰、足
            {null, null, null, null},
            //アーマー内発光：頭、胴、腰、足
            {null, null, null, null},
            //アーマー外発光：頭、胴、腰、足
            {null, null, null, null}
    };
    private final ModelMultiBase[] models = new ModelMultiBase[]{
            //基本 防具内 防具外
            null, null, null
    };
    private final TextureBox[] textureBox = new TextureBox[]{
            //基本 防具
            null, null
    };
    private final LivingEntity owner;
    private byte color;
    private boolean contract;

    public DefaultMultiModel(LivingEntity owner, TextureBox defaultMainBoxes, TextureBox defaultArmorBoxes) {
        this.owner = owner;
        this.defaultMainBoxes = defaultMainBoxes;
        this.defaultArmorBoxes = defaultArmorBoxes;
    }

    public void updateTextures() {
        //テクスチャ指定
        if (textureBox[0] == null) {
            textureBox[0] = defaultMainBoxes;
        }
        if (FMLEnvironment.dist.isClient()) {
            TextureBox mainBox = textureBox[0];
            int mainColor = (color & 0x00ff) + (contract ? 0 : ModelManager.tx_wild);
            int growColor = (color & 0x00ff) + (contract ? ModelManager.tx_eyecontract : ModelManager.tx_eyewild);
            if (!mainBox.hasColor(mainColor)) {//クライアントに存在しない色が指定された場合、読み込める別の色を読み込む。
                mainColor = contract ? mainBox.getRandomContractColor(owner.world.rand) : mainBox.getRandomWildColor(owner.world.rand);
                setColor((byte) mainColor);
            }
            if (mainBox.hasColor(mainColor)) {
                textures[0][0] = mainBox.getTextureName(mainColor);
                textures[0][1] = mainBox.getTextureName(growColor);
                models[0] = mainBox.models[0];
            }
        }
        //防具テクスチャ指定
        if (textureBox[1] == null) {
            textureBox[1] = defaultArmorBoxes;
        }
        if (FMLEnvironment.dist.isClient()) {
            TextureBox armorBox = textureBox[1];
            for (int i = 0; i < 4; i++) {//頭～足で4回
                String material = "default";
                int damage = 0;
                ItemStack armorStack = owner.getItemStackFromSlot(
                        EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, 3 - i));
                Item armor = armorStack.getItem();
                if (armor instanceof ArmorItem) {
                    //クライアント専用メソッド、注意
                    material = getArmorMaterialName((ArmorItem) armor);
                    damage = armorStack.getDamage();
                }
                textures[1][i] = armorBox.getArmorTextureName(ModelManager.tx_armor1, material, damage);
                textures[2][i] = armorBox.getArmorTextureName(ModelManager.tx_armor2, material, damage);
                textures[3][i] = armorBox.getArmorTextureName(ModelManager.tx_armor1light, material, damage);
                textures[4][i] = armorBox.getArmorTextureName(ModelManager.tx_armor2light, material, damage);
            }
            models[1] = armorBox.models[1];
            models[2] = armorBox.models[2];
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static String getArmorMaterialName(ArmorItem armor) {
        return armor.getArmorMaterial().getName();
    }

    //鯖蔵同期
    public void sync() {
        if (!owner.world.isRemote) {
            Networking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> owner),
                    new PacketSyncModel(owner.getEntityId(), textureBox[0].textureName, textureBox[1].textureName, color, contract));
        } else {
            Networking.INSTANCE.sendToServer(new PacketSyncModel(owner.getEntityId(), textureBox[0].textureName, textureBox[1].textureName, color, contract));
        }
    }

    public void write(CompoundNBT compound) {
        compound.putByte("ModelColor", this.getColor());
        compound.putBoolean("IsContract", this.getContract());
        TextureBox[] boxes = this.getTextureBox();
        if (boxes != null) {
            if (boxes[0] != null) {
                compound.putString("MaidModel", this.getTextureBox()[0].textureName);
            }
            if (boxes[1] != null) {
                compound.putString("ArmorModel", this.getTextureBox()[1].textureName);
            }

        }
    }

    //サーバーでしか動かんので注意
    public void read(CompoundNBT compound) {
        //デフォ値から変えるとマズい
        if (compound.contains("ModelColor")) {
            this.setColor(compound.getByte("ModelColor"));
        }
        if (compound.contains("IsContract")) {
            this.setContract(compound.getBoolean("IsContract"));
        }
        if (compound.contains("MaidModel")) {
            this.setTextureBox(0, ModelManager.instance.getTextureBox(compound.getString("MaidModel")));
        } else {
            this.setTextureBox(0, defaultMainBoxes);
        }
        if (compound.contains("ArmorModel")) {
            this.setTextureBox(1, ModelManager.instance.getTextureBox(compound.getString("ArmorModel")));
        } else {
            this.setTextureBox(1, defaultArmorBoxes);
        }
        updateTextures();
    }

    //セッター / ゲッター

    @Override
    public void setTextures(int index, ResourceLocation[] names) {
        this.textures[index] = names;
    }

    @Override
    public ResourceLocation[] getTextures(int index) {
        return this.textures[index];
    }

    @Override
    public void setMultiModels(int index, ModelMultiBase models) {
        this.models[index] = models;
    }

    @Override
    public ModelMultiBase[] getMultiModels() {
        return this.models;
    }

    public void setColor(byte color) {
        this.color = color;
    }

    public byte getColor() {
        return this.color;
    }

    public void setContract(boolean isContract) {
        this.contract = isContract;
    }

    public boolean getContract() {
        return this.contract;
    }

    public void setTextureBox(int index, @Nullable TextureBox textureBox) {
        this.textureBox[index] = textureBox;
    }

    public TextureBox[] getTextureBox() {
        return this.textureBox;
    }

    @Override
    public boolean canRenderArmor(int index) {
        return !owner.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, index)).isEmpty();
    }

    @Override
    public void setCanRenderArmor(int index, boolean canRender) {

    }
}
