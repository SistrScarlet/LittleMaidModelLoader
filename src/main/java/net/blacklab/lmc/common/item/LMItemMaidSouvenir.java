package net.blacklab.lmc.common.item;

import net.blacklab.lmc.common.entity.LMEntityItemAntiDamage;
import net.blacklab.lmc.common.helper.LittleMaidHelper;
import net.blacklab.lmr.entity.littlemaid.EntityLittleMaid;
import net.blacklab.lmr.setup.Registration;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import static net.blacklab.lmr.util.Statics.dataWatch_Flags_remainsContract;

/**
 * メイドの土産
 *
 * @author computer
 */
public class LMItemMaidSouvenir extends Item {


    public LMItemMaidSouvenir() {
        super(new Item.Properties().maxStackSize(1));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();

        if (!stack.isEmpty()
                && stack.getItem() == Registration.MAID_SOUVENIR.get()
                && stack.hasTag()) {

            BlockPos position = pos.offset(facing);
            double x = position.getX() + 0.5;
            double y = position.getY();
            double z = position.getZ() + 0.5;

            //メイドさんのスポーン
            Entity entity = LittleMaidHelper.spawnEntityFromItemStack(stack, world, x, y, z);

            //ストライキ状態にする
            if (entity != null) {
                EntityLittleMaid maid = (EntityLittleMaid) entity;

                //契約時間をリセット
                maid.clearMaidContractLimit();
                //ストライキを設定する
                maid.setMaidFlags(true, dataWatch_Flags_remainsContract);

            }

            //アイテムを消費
            //クリエイティブでも消費させる
            //stack.shrink(1);
            player.setHeldItem(hand, ItemStack.EMPTY);

            //1秒のCoolDownTimeを設定
            player.getCooldownTracker().setCooldown(this, 20 * 1);

            return ActionResultType.SUCCESS;

        }

        return ActionResultType.PASS;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new StringTextComponent(TextFormatting.LIGHT_PURPLE + I18n.format("item.maid_souvenir.info")));

        if (stack.hasTag()) {

            //OwnerId
            if (stack.getOrCreateTag().contains("maid_owner")) {
                tooltip.add(new StringTextComponent("Owner : " + stack.getOrCreateTag().getString("maid_owner")));
            }
            //メイド名
            if (stack.getOrCreateTag().contains("maid_name")) {
                tooltip.add(new StringTextComponent("Maid : " + stack.getOrCreateTag().getString("maid_name")));
            }

        }
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    //ログイン時にアンチダメージアイテムエンティティに変換
    @Override
    @Nullable
    public Entity createEntity(World world, Entity replace, ItemStack itemstack) {
        ItemEntity souvenir = new LMEntityItemAntiDamage(world, replace.getPosX(), replace.getPosY(), replace.getPosZ(), itemstack);
        souvenir.setDefaultPickupDelay();

        souvenir.setMotion(replace.getMotion());

        return souvenir;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return stack.hasTag();
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return stack.hasTag() ? Rarity.RARE : Rarity.COMMON;
    }
}
