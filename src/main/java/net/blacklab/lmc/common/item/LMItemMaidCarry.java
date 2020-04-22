package net.blacklab.lmc.common.item;

import net.blacklab.lmc.common.entity.LMEntityItemAntiDamage;
import net.blacklab.lmc.common.helper.LittleMaidHelper;
import net.blacklab.lmr.entity.littlemaid.EntityLittleMaid;
import net.blacklab.lmr.setup.ModSetup;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
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

/**
 * Mob持ち運び用アイテム
 *
 * @author computer
 */
public class LMItemMaidCarry extends Item {

    /**
     * コンストラクタ
     */
    public LMItemMaidCarry() {
        super(new Item.Properties()
                .maxStackSize(1)
                .group(ModSetup.ITEM_GROUP));
    }

    /**
     * 左クリックからのアイテム化
     */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        //メイドさんアイテム化
        return createMaidItemStack(stack, player, entity);
    }

    /**
     * Shift＋右クリックからのアイテム化
     */
    @Override
    public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        //メイドさんアイテム化
        boolean ret = createMaidItemStack(stack, playerIn, target);
        if (ret && stack.hasTag()) {
            playerIn.setHeldItem(hand, stack);
        }
        return ret;
    }

    /**
     * メイドさんを生成する
     */
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        World world = context.getWorld();

        if (!stack.isEmpty()
                && stack.getItem() instanceof LMItemMaidCarry
                && stack.hasTag()) {

            BlockPos position = pos.offset(facing);
            double x = position.getX() + 0.5;
            double y = position.getY();
            double z = position.getZ() + 0.5;

            //メイドさんのスポーン
            LittleMaidHelper.spawnEntityFromItemStack(stack, world, x, y, z);

            //Tag情報を初期化
            stack.setTag(null);

            return ActionResultType.SUCCESS;

        }

        return ActionResultType.PASS;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new StringTextComponent(TextFormatting.LIGHT_PURPLE + I18n.format("item.maid_carry.info")));

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

    /**
     * 耐性EntityItemを利用する
     */
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    /**
     * 耐性EntityItemを生成する
     * voidダメージ以外は無効化する
     */
    @Override
    @Nullable
    public Entity createEntity(World world, Entity replace, ItemStack itemstack) {
        ItemEntity souvenir = new LMEntityItemAntiDamage(world, replace.getPosX(), replace.getPosY(), replace.getPosZ(), itemstack);
        souvenir.setDefaultPickupDelay();

        souvenir.setMotion(replace.getMotion());

        return souvenir;
    }

    /**
     * NBTタグを持つ場合にエフェクト表示
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return stack.hasTag();
    }

    /**
     * メイドさんをアイテム化
     *
     * @return
     */
    public boolean createMaidItemStack(ItemStack stack, PlayerEntity player, Entity entity) {

        //メイドさんチェック
        if (!(entity instanceof EntityLittleMaid)) {
            return false;
        }

        EntityLittleMaid entityMaid = (EntityLittleMaid) entity;

        //契約メイドさんチェック
        if (!player.getUniqueID().equals(entityMaid.getMaidMasterUUID())) {
            return true;
        }

        //NBTがある場合は何もしない
        if (stack.hasTag()) {
            return true;
        }

        //メイド用スポーン情報の書き込み
        LittleMaidHelper.getItemStackFromEntity(entityMaid, stack);

        //メイドさん消去
        entityMaid.setDead();

        return true;
    }

}
