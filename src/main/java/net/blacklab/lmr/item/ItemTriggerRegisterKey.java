package net.blacklab.lmr.item;

import net.blacklab.lmr.LittleMaidReengaged;
import net.blacklab.lmr.entity.littlemaid.trigger.ModeTrigger;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTriggerRegisterKey extends Item {

    public static final String RK_MODE_TAG = LittleMaidReengaged.DOMAIN + ":RK_MODE";
    public static final String RK_COUNT = LittleMaidReengaged.DOMAIN + ":RK_COUNT";

    public static final int RK_MAX_COUNT = 32;

    public ItemTriggerRegisterKey() {
        super(new Item.Properties());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        CompoundNBT tagCompound = stack.getOrCreateTag();

        String modeString = tagCompound.getString(RK_MODE_TAG);

        // 登録モードを切り替える．
        int index = ModeTrigger.getSelectorList().indexOf(modeString);
        if (index < 0 || index >= ModeTrigger.getSelectorList().size()) {
            index = 0;
        }

//		modeString = TriggerSelect.selector.get(index);
        tagCompound.putString(RK_MODE_TAG, modeString);

        if (!worldIn.isRemote) {
            playerIn.sendMessage(new TranslationTextComponent("littleMaidMob.chat.text.changeregistermode", modeString));
        }

        return ActionResult.resultSuccess(stack);
    }

    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        CompoundNBT tagCompound = stack.getOrCreateTag();
        tooltip.add(new StringTextComponent("Mode: " + tagCompound.getString(RK_MODE_TAG)));
        tooltip.add(new StringTextComponent("Remains: " + (RK_MAX_COUNT - tagCompound.getInt(RK_COUNT))));
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (!stack.hasTag()) {
			if (ModeTrigger.getSelectorList().size() <= 0) {
				return;
			}
			CompoundNBT tag = new CompoundNBT();
			tag.putString(RK_MODE_TAG, ModeTrigger.getSelectorList().get(0));
			stack.setTag(tag);
		}
	}

}
