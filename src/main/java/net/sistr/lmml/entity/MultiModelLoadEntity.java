package net.sistr.lmml.entity;


import net.blacklab.lmr.entity.maidmodel.IHasMultiModel;
import net.blacklab.lmr.entity.maidmodel.ModelMultiBase;
import net.blacklab.lmr.entity.maidmodel.TextureBox;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.sistr.lmml.client.ModelSelectScreen;
import net.sistr.lmml.setup.Registration;
import net.sistr.lmml.util.manager.ModelManager;

import javax.annotation.Nullable;

//実装例みたいな
//こいつを直接継承はしないでくだちい
public class MultiModelLoadEntity extends CreatureEntity implements IHasMultiModel, IEntityAdditionalSpawnData {
    private final DefaultMultiModel comp = new DefaultMultiModel(this,
            ModelManager.instance.getDefaultTexture(this),
            ModelManager.instance.getDefaultTexture(this));

    public MultiModelLoadEntity(EntityType<MultiModelLoadEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public MultiModelLoadEntity(World world) {
        super(Registration.MULTI_MODEL_LOAD_ENTITY.get(), world);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
    }

    //@Override
    //protected void registerAttributes() {
    //    super.registerAttributes();
    //    this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23);
    //}

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MobEntity.func_233666_p_().func_233815_a_(Attributes.field_233821_d_, 0.3F)
                .func_233815_a_(Attributes.field_233818_a_, 8.0D)
                .func_233815_a_(Attributes.field_233823_f_, 2.0D);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        comp.write(compound);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        comp.read(compound);
    }

    //鯖
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        TextureBox[] box = comp.getTextureBox();
        buffer.writeString(box[0].textureName);
        buffer.writeString(box[1].textureName);
        buffer.writeByte(getColor());
        buffer.writeBoolean(getContract());
    }

    //蔵
    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        setTextureBox(0, ModelManager.instance.getTextureBox(additionalData.readString()));
        setTextureBox(1, ModelManager.instance.getTextureBox(additionalData.readString()));
        setColor(additionalData.readByte());
        setContract(additionalData.readBoolean());
        updateTextures();
    }

    //@Override
    protected ActionResultType func_230254_b_(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.getItem() == Items.CAKE) {
            if (getContract()) {
                return ActionResultType.PASS;
            }
            if (player.world.isRemote) {
                return ActionResultType.CONSUME;
            }
            setContract(true);
            if (!player.abilities.isCreativeMode) {
                itemstack.shrink(1);
                if (itemstack.isEmpty()) {
                    player.inventory.deleteStack(itemstack);
                }
            }
            updateTextures();
            sync();
            return ActionResultType.CONSUME;
        }
        if (itemstack.getItem().isIn(ItemTags.WOOL)) {
            if (!getContract()) {
                return ActionResultType.PASS;
            }
            if (player.world.isRemote) {
                return ActionResultType.CONSUME;
            }
            TextureBox[] box = new TextureBox[2];
            box[0] = box[1] = ModelManager.instance.getTextureBox(ModelManager.instance.getRandomTextureString(getRNG()));
            setColor((byte) box[0].getRandomContractColor(rand));
            setTextureBox(0, box[0]);
            setTextureBox(1, box[1]);
            updateTextures();
            sync();
            return ActionResultType.CONSUME;
        }
        if (itemstack.getItem().isIn(Tags.Items.DYES)) {
            if (!getContract()) {
                return ActionResultType.PASS;
            }
            if (player.world.isRemote) {
                return ActionResultType.CONSUME;
            }
            setColor((byte) getTextureBox()[0].getRandomContractColor(getRNG()));
            updateTextures();
            sync();
            return ActionResultType.CONSUME;
        }
        if (!itemstack.isEmpty() && (itemstack.getItem() instanceof ArmorItem || itemstack.getEquipmentSlot() != null)) {
            if (player.world.isRemote) {
                return ActionResultType.CONSUME;
            }
            this.setItemStackToSlot(MobEntity.getSlotForItemStack(itemstack), itemstack);
            if (!player.abilities.isCreativeMode) {
                itemstack.shrink(1);
                if (itemstack.isEmpty()) {
                    player.inventory.deleteStack(itemstack);
                }
            }
            return ActionResultType.CONSUME;
        }
        if (player.world.isRemote) {
            openScreen(player);
        }
        return ActionResultType.PASS;
    }

    //GUI開くやつ
    //未実装
    @OnlyIn(Dist.CLIENT)
    public void openScreen(PlayerEntity player) {
        Minecraft.getInstance().displayGuiScreen(new ModelSelectScreen(new StringTextComponent("test"), this, this, ~0));
    }

    public void sync() {
        comp.sync();
    }

    @Override
    public void updateTextures() {
        comp.updateTextures();
    }

    @Override
    public void setTextures(int index, ResourceLocation[] names) {
        comp.setTextures(index, names);
    }

    @Override
    public ResourceLocation[] getTextures(int index) {
        return comp.getTextures(index);
    }

    @Override
    public void setMultiModels(int index, ModelMultiBase models) {
        comp.setMultiModels(index, models);
    }

    @Override
    public ModelMultiBase[] getMultiModels() {
        return comp.getMultiModels();
    }

    @Override
    public void setColor(byte color) {
        comp.setColor(color);
    }

    @Override
    public byte getColor() {
        return comp.getColor();
    }

    @Override
    public void setContract(boolean isContract) {
        comp.setContract(isContract);
    }

    @Override
    public boolean getContract() {
        return comp.getContract();
    }

    @Override
    public void setTextureBox(int index, @Nullable TextureBox textureBox) {
        comp.setTextureBox(index, textureBox);
    }

    @Override
    public TextureBox[] getTextureBox() {
        return comp.getTextureBox();
    }

    @Override
    public boolean canRenderArmor(int index) {
        return comp.canRenderArmor(index);
    }

    @Override
    public void setCanRenderArmor(int index, boolean canRender) {
        comp.setCanRenderArmor(index, canRender);
    }

    //オーバーライドしなくても動くが、IEntityAdditionalSpawnDataが機能しない
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
