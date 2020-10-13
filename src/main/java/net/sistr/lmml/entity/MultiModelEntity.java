package net.sistr.lmml.entity;


import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.client.ModelSelectScreen;
import net.sistr.lmml.maidmodel.IModelCaps;
import net.sistr.lmml.maidmodel.ModelMultiBase;
import net.sistr.lmml.util.TextureColor;
import net.sistr.lmml.util.TextureHolder;
import net.sistr.lmml.resource.manager.TextureManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

//テスト用エンティティ
public class MultiModelEntity extends CreatureEntity implements IHasMultiModel, IEntityAdditionalSpawnData {
    private final MultiModelCompound multiModel;

    public MultiModelEntity(EntityType<MultiModelEntity> type, World worldIn) {
        super(type, worldIn);
        multiModel = new MultiModelCompound(this,
                LittleMaidModelLoader.getInstance().getTextureManager().getTexture("default")
                        .orElseThrow(() -> new IllegalStateException("デフォルトモデルが存在しません。")),
                LittleMaidModelLoader.getInstance().getTextureManager().getTexture("default")
                        .orElseThrow(() -> new IllegalStateException("デフォルトモデルが存在しません。")));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return LivingEntity.registerAttributes().createMutableAttribute(Attributes.FOLLOW_RANGE, 16.0D)
                .createMutableAttribute(Attributes.ATTACK_KNOCKBACK);
    }

    @Override
    public void writeAdditional(@Nonnull CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putByte("SkinColor", (byte) getColor().getIndex());
        compound.putBoolean("IsContract", isContract());
        compound.putString("SkinTexture", getTextureHolder(Layer.SKIN, Part.HEAD).getTextureName());
        for (Part part : Part.values()) {
            compound.putString("ArmorTextureInner" + part.getPartName(),
                    getTextureHolder(Layer.INNER, part).getTextureName());
            compound.putString("ArmorTextureOuter" + part.getPartName(),
                    getTextureHolder(Layer.OUTER, part).getTextureName());
        }
    }

    @Override
    public void readAdditional(@Nonnull CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.contains("SkinColor")) {
            setColor(TextureColor.getColor(compound.getByte("SkinColor")));
        }
        setContract(compound.getBoolean("IsContract"));
        TextureManager textureManager = LittleMaidModelLoader.getInstance().getTextureManager();
        if (compound.contains("SkinTexture")) {
            textureManager.getTexture(compound.getString("SkinTexture"))
                    .ifPresent(textureHolder -> setTextureHolder(textureHolder, Layer.SKIN, Part.HEAD));
        }
        for (Part part : Part.values()) {
            String inner = "ArmorTextureInner" + part.getPartName();
            String outer = "ArmorTextureOuter" + part.getPartName();
            if (compound.contains(inner)) {
                textureManager.getTexture(compound.getString(inner))
                        .ifPresent(textureHolder -> setTextureHolder(textureHolder, Layer.INNER, part));
            }
            if (compound.contains(outer)) {
                textureManager.getTexture(compound.getString(outer))
                        .ifPresent(textureHolder -> setTextureHolder(textureHolder, Layer.OUTER, part));
            }
        }
    }

    //ここはForge機能なのでFabric移植時に改変が必要

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeEnumValue(getColor());
        buffer.writeBoolean(isContract());
        buffer.writeString(getTextureHolder(Layer.SKIN, Part.HEAD).getTextureName());
        for (Part part : Part.values()) {
            buffer.writeString(getTextureHolder(Layer.INNER, part).getTextureName());
            buffer.writeString(getTextureHolder(Layer.OUTER, part).getTextureName());
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        //readString()はクラ処理。このメソッドでは、クラ側なので問題なし
        setColor(additionalData.readEnumValue(TextureColor.class));
        setContract(additionalData.readBoolean());
        TextureManager textureManager = LittleMaidModelLoader.getInstance().getTextureManager();
        textureManager.getTexture(additionalData.readString())
                .ifPresent(textureHolder -> setTextureHolder(textureHolder, Layer.SKIN, Part.HEAD));
        for (Part part : Part.values()) {
            textureManager.getTexture(additionalData.readString())
                    .ifPresent(textureHolder -> setTextureHolder(textureHolder, Layer.INNER, part));
            textureManager.getTexture(additionalData.readString())
                    .ifPresent(textureHolder -> setTextureHolder(textureHolder, Layer.OUTER, part));
        }
    }
    
    @Override
    protected ActionResultType func_230254_b_(PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem) stack.getItem();
            this.setItemStackToSlot(armor.getEquipmentSlot(), stack);
            return ActionResultType.func_233537_a_(this.world.isRemote);
        }
        if (world.isRemote) {
            openGUI();
        }
        return super.func_230254_b_(player, hand);
    }

    @OnlyIn(Dist.CLIENT)
    public void openGUI() {
        Minecraft.getInstance().displayGuiScreen(
                new ModelSelectScreen(new StringTextComponent("aaa"), this.world, this));
    }

    //このままだとEntitySizeが作っては捨てられてを繰り返すのでパフォーマンスはよろしくない
    @Nonnull
    @Override
    public EntitySize getSize(@Nonnull Pose poseIn) {
        EntitySize size;
        ModelMultiBase model = getModel(Layer.SKIN, Part.HEAD)
                .orElse(LittleMaidModelLoader.getInstance().getModelManager().getDefaultModel());
        float height = model.getHeight(getCaps());
        float width = model.getWidth(getCaps());
        size = new EntitySize(width, height, false);
        return size.scale(getRenderScale());
    }

    //上になんか乗ってるやつのオフセット
    @Override
    public double getMountedYOffset() {
        ModelMultiBase model = getModel(Layer.SKIN, Part.HEAD)
                .orElse(LittleMaidModelLoader.getInstance().getModelManager().getDefaultModel());
        return model.getMountedYOffset(getCaps());
    }

    //騎乗時のオフセット
    @Override
    public double getYOffset() {
        ModelMultiBase model = getModel(Layer.SKIN, Part.HEAD)
                .orElse(LittleMaidModelLoader.getInstance().getModelManager().getDefaultModel());
        return model.getyOffset(getCaps()) - getHeight();
    }

    //防具の更新
    @Override
    public void setItemStackToSlot(EquipmentSlotType slotIn, @Nonnull ItemStack stack) {
        if (slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            multiModel.updateArmor();
        }
        super.setItemStackToSlot(slotIn, stack);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Optional<ResourceLocation> getTexture(@Nonnull Layer layer, @Nonnull Part part, boolean isLight) {
        return multiModel.getTexture(layer, part, isLight);
    }

    @Override
    public void setTextureHolder(TextureHolder textureHolder, @Nonnull Layer layer, @Nonnull Part part) {
        multiModel.setTextureHolder(textureHolder, layer, part);
    }

    @Override
    public TextureHolder getTextureHolder(@Nonnull Layer layer, @Nonnull Part part) {
        return multiModel.getTextureHolder(layer, part);
    }

    @Override
    public void setColor(TextureColor color) {
        multiModel.setColor(color);
    }

    @Override
    public TextureColor getColor() {
        return multiModel.getColor();
    }

    @Override
    public void setContract(boolean isContract) {
        multiModel.setContract(isContract);
    }

    @Override
    public boolean isContract() {
        return multiModel.isContract();
    }

    @Override
    public Optional<ModelMultiBase> getModel(@Nonnull Layer layer, @Nonnull Part part) {
        return multiModel.getModel(layer, part);
    }

    @Nonnull
    @Override
    public IModelCaps getCaps() {
        return multiModel.getCaps();
    }

    @Override
    public boolean isArmorVisible(Part part) {
        return multiModel.isArmorVisible(part);
    }

    @Override
    public boolean isArmorGlint(Part part) {
        return multiModel.isArmorGlint(part);
    }

    @Override
    public boolean isAllowChangeTexture(@Nullable Entity changer, TextureHolder textureHolder, @Nonnull Layer layer, @Nonnull Part part) {
        return true;
    }

    //IEntityAdditionalSpawnDataに要る
    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
