package net.blacklab.lmr.item;

import net.blacklab.lmr.LittleMaidReengaged;
import net.blacklab.lmr.entity.experience.ExperienceUtil;
import net.blacklab.lmr.entity.littlemaid.EntityLittleMaid;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMaidPorter extends Item {
    public ItemMaidPorter() {
        super(new Item.Properties()
                .maxStackSize(1));
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        CompoundNBT stackTag = stack.getTag();
        if (stackTag != null) {
            String customName = stackTag.getString(LittleMaidReengaged.DOMAIN + ":MAID_NAME");
            float experience = stackTag.getFloat(LittleMaidReengaged.DOMAIN + ":EXPERIENCE");

            if (!customName.isEmpty()) {
                tooltip.add(new StringTextComponent("Name: ".concat(customName)));
            }
            tooltip.add(new StringTextComponent(String.format("Level: %3d", ExperienceUtil.getLevelFromExp(experience))));
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        if (world.isRemote) {
            return ActionResultType.PASS;
        }
        CompoundNBT tagCompound = stack.getOrCreateTag();
        BlockPos pos = context.getPos();
        if (world.isAirBlock(pos.add(0, 1, 0)) && world.isAirBlock(pos.add(0, 2, 0))) {
            String customName = tagCompound.getString(LittleMaidReengaged.DOMAIN + ":MAID_NAME");
            float experience = tagCompound.getFloat(LittleMaidReengaged.DOMAIN + ":EXPERIENCE");

            EntityLittleMaid lMaid = new EntityLittleMaid(world) {
                @Deprecated
                public EntityLittleMaid addMaidExperienceWithoutEvent(float value) {
                    maidExperience += value;
                    return this;
                }
            }.addMaidExperienceWithoutEvent(experience);
            lMaid.setLocationAndAngles(pos.getX(), pos.getY() + 1, pos.getZ(), 0, 0);
            world.addEntity(lMaid);
            lMaid.processInteract(player, Hand.MAIN_HAND);

            if (!customName.isEmpty()) {
                lMaid.setCustomName(new StringTextComponent(customName));
            }
            lMaid.maidInventory.clear();
            lMaid.maidInventory.readFromNBT(tagCompound.getList(LittleMaidReengaged.DOMAIN + ":MAID_INVENTORY", 10));

            lMaid.setTextureNameMain(tagCompound.getString(LittleMaidReengaged.DOMAIN + ":MAIN_MODEL_NAME"));
            lMaid.setTextureNameArmor(tagCompound.getString(LittleMaidReengaged.DOMAIN + ":ARMOR_MODEL_NAME"));
            lMaid.setColor((byte) tagCompound.getInt(LittleMaidReengaged.DOMAIN + ":MAID_COLOR"));
        } else {
            return ActionResultType.PASS;
        }
        player.setItemStackToSlot(hand == Hand.OFF_HAND ? EquipmentSlotType.OFFHAND : EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
        return ActionResultType.SUCCESS;
    }

}
