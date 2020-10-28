package net.sistr.lmml.entity;


import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
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
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.sistr.lmml.client.ModelSelectScreen;
import net.sistr.lmml.entity.compound.IHasMultiModel;
import net.sistr.lmml.entity.compound.MultiModelCompound;
import net.sistr.lmml.entity.compound.SoundPlayable;
import net.sistr.lmml.entity.compound.SoundPlayableCompound;
import net.sistr.lmml.maidmodel.IModelCaps;
import net.sistr.lmml.maidmodel.ModelMultiBase;
import net.sistr.lmml.resource.holder.ConfigHolder;
import net.sistr.lmml.resource.holder.TextureHolder;
import net.sistr.lmml.resource.manager.LMModelManager;
import net.sistr.lmml.resource.manager.LMTextureManager;
import net.sistr.lmml.resource.util.LMSounds;
import net.sistr.lmml.resource.util.TextureColors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

//テスト用エンティティ
public class MultiModelEntity extends CreatureEntity implements IHasMultiModel, SoundPlayable, IEntityAdditionalSpawnData {
    private final MultiModelCompound multiModel;
    private final SoundPlayableCompound soundPlayer;

    public MultiModelEntity(EntityType<MultiModelEntity> type, World worldIn) {
        super(type, worldIn);
        multiModel = new MultiModelCompound(this,
                LMTextureManager.INSTANCE.getTexture("default")
                        .orElseThrow(() -> new IllegalStateException("デフォルトモデルが存在しません。")),
                LMTextureManager.INSTANCE.getTexture("default")
                        .orElseThrow(() -> new IllegalStateException("デフォルトモデルが存在しません。")));
        soundPlayer = new SoundPlayableCompound(this,
                () -> multiModel.getTextureHolder(Layer.SKIN, Part.HEAD).getTextureName());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
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
            setColor(TextureColors.getColor(compound.getByte("SkinColor")));
        }
        setContract(compound.getBoolean("IsContract"));
        LMTextureManager textureManager = LMTextureManager.INSTANCE;
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
        setColor(additionalData.readEnumValue(TextureColors.class));
        setContract(additionalData.readBoolean());
        LMTextureManager textureManager = LMTextureManager.INSTANCE;
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
    protected boolean processInteract(PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem) stack.getItem();
            this.setItemStackToSlot(armor.getEquipmentSlot(), stack);
            return true;
        }
        if (world.isRemote) {
            openGUI();
        } else {
            play(LMSounds.LIVING_DAYTIME);
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void openGUI() {
        Minecraft.getInstance().displayGuiScreen(
                new ModelSelectScreen(new StringTextComponent(""), this.world, this));
    }

    //このままだとEntitySizeが作っては捨てられてを繰り返すのでパフォーマンスはよろしくない
    @Nonnull
    @Override
    public EntitySize getSize(@Nonnull Pose poseIn) {
        EntitySize size;
        ModelMultiBase model = getModel(Layer.SKIN, Part.HEAD)
                .orElse(LMModelManager.INSTANCE.getDefaultModel());
        float height = model.getHeight(getCaps());
        float width = model.getWidth(getCaps());
        size = new EntitySize(width, height, false);
        return size.scale(getRenderScale());
    }

    //上になんか乗ってるやつのオフセット
    @Override
    public double getMountedYOffset() {
        ModelMultiBase model = getModel(Layer.SKIN, Part.HEAD)
                .orElse(LMModelManager.INSTANCE.getDefaultModel());
        return model.getMountedYOffset(getCaps());
    }

    //騎乗時のオフセット
    @Override
    public double getYOffset() {
        ModelMultiBase model = getModel(Layer.SKIN, Part.HEAD)
                .orElse(LMModelManager.INSTANCE.getDefaultModel());
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
    public void setColor(TextureColors color) {
        multiModel.setColor(color);
    }

    @Override
    public TextureColors getColor() {
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

    @Override
    public void play(String soundName) {
        soundPlayer.play(soundName);
    }

    @Override
    public void setConfigHolder(ConfigHolder configHolder) {
        soundPlayer.setConfigHolder(configHolder);
    }

    @Override
    public ConfigHolder getConfigHolder() {
        return soundPlayer.getConfigHolder();
    }

    //IEntityAdditionalSpawnDataに要る
    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
