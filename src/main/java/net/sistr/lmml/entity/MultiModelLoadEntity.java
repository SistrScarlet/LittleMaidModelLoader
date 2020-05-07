package net.sistr.lmml.entity;


import net.blacklab.lmr.entity.maidmodel.*;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.setup.Registration;
import net.sistr.lmml.util.manager.ModelManager;

public class MultiModelLoadEntity extends CreatureEntity implements IModelEntity {
    /**
     * メイドカラー(byte)
     */
    protected static final DataParameter<Byte> TEXTURE_COLOR = EntityDataManager.createKey(MultiModelLoadEntity.class, DataSerializers.BYTE);
    /**
     * テクスチャ関連のデータを管理
     **/
    private final ModelConfigCompound textureData;

    public EntityCaps caps;//Client側のみ
    //モデル
    protected String textureNameMain;
    protected String textureNameArmor;

    public MultiModelLoadEntity(EntityType<MultiModelLoadEntity> type, World worldIn) {
        super(type, worldIn);
        //モデルレンダリング用のフラグ獲得用ヘルパー関数
        caps = new EntityCaps(this);
        textureData = new ModelConfigCompound(this, caps);
        // 形態形成場
        textureData.setColor((byte) 0xc);
        TextureBox[] ltb = new TextureBox[2];
        ltb[0] = ltb[1] = ModelManager.instance.getDefaultTexture(this);
        setTexturePackName(ltb);
    }

    public MultiModelLoadEntity(World world) {
        super(Registration.MULTI_MODEL_LOAD_ENTITY.get(), world);
        //モデルレンダリング用のフラグ獲得用ヘルパー関数
        caps = new EntityCaps(this);
        textureData = new ModelConfigCompound(this, caps);
        // 形態形成場
        textureData.setColor((byte) 0xc);
        TextureBox[] ltb = new TextureBox[2];
        ltb[0] = ltb[1] = ModelManager.instance.getDefaultTexture(this);
        setTexturePackName(ltb);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));

    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(TEXTURE_COLOR, (byte) 0);
    }

    @Override
    protected boolean processInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.getItem() == Items.STICK) {
            if (textureData.isContract()) {
                TextureBox[] box = new TextureBox[2];
                box[0] = box[1] = ModelManager.instance.getTextureBox(ModelManager.instance.getRandomTextureString(getRNG()));
                textureData.setColor((byte) box[0].getRandomContractColor(getRNG()));
                setTexturePackName(box);
            }
            textureData.setContract(true);
            return true;
        }
        if (!itemstack.isEmpty()) {
            this.setItemStackToSlot(MobEntity.getSlotForItemStack(itemstack), itemstack);
            if (!player.abilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            if (player.world.isRemote) {
                setTextureNames();
            }

            return true;
        }
        return false;
    }

    @Override
    public void setTexturePackName(TextureBox[] pTextureBox) {
        // Client
        textureData.setTexturePackName(pTextureBox);
        setTextureNames();
        LittleMaidModelLoader.Debug("ID:%d, TextureModel:%s", getEntityId(), textureData.getTextureName(0));
        // モデルの初期化
        ((TextureBox) textureData.textureBox[0]).models[0].setCapsValue(IModelCaps.caps_changeModel, caps);
    }

    @Override
    public void setColor(byte index) {
        textureData.setColor(index);
        dataManager.set(TEXTURE_COLOR, index);
    }

    @Override
    public byte getColor() {
        return dataManager.get(TEXTURE_COLOR);
    }

    @Override
    public void setContract(boolean flag) {
        textureData.setContract(flag);
    }

    @Override
    public boolean isContract() {
        return false;
    }

    /**
     * Client用
     */
    public void setTextureNames() {
        textureData.setTextureNames();
        if (getEntityWorld().isRemote) {
            textureNameMain = textureData.getTextureName(0);
            textureNameArmor = textureData.getTextureName(1);
        }
    }

    //textureEntity
    @Override
    public void setTextureBox(TextureBoxBase[] pTextureBox) {
        textureData.setTextureBox(pTextureBox);
    }

    @Override
    public TextureBoxBase[] getTextureBox() {
        return textureData.getTextureBox();
    }

    @Override
    public void setTextures(int pIndex, ResourceLocation[] pNames) {
        textureData.setTextures(pIndex, pNames);
    }

    @Override
    public ResourceLocation[] getTextures(int pIndex) {
        return textureData.getTextures(pIndex);
    }

    @Override
    public ModelConfigCompound getModelConfigCompound() {
        return textureData;
    }
}
