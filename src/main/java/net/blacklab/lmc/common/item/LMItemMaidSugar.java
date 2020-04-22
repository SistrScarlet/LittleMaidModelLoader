package net.blacklab.lmc.common.item;

import net.blacklab.lmc.common.helper.LittleMaidHelper;
import net.blacklab.lmr.config.LMRConfig;
import net.blacklab.lmr.entity.littlemaid.EntityLittleMaid;
import net.blacklab.lmr.network.LMRNetwork;
import net.blacklab.lmr.setup.ModSetup;
import net.blacklab.lmr.setup.Registration;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

//EntityList -> EntityType
/**
 * メイドシュガー
 */
public class LMItemMaidSugar extends Item {

    /**
     * EntityDataNBT保存用キー
     */
    public static String ANIMAL_MAID_KEY = "animalLittleMaid";

    public LMItemMaidSugar() {
        super(new Item.Properties()
                .group(ModSetup.ITEM_GROUP));
    }


    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        return metamorphoseMaid(entity, player, Hand.MAIN_HAND);
    }

    /**
     * Shift＋右クリックからのアイテム化
     */
    @Override
    public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        return metamorphoseMaid(target, playerIn, hand);
    }


    /**
     * リトルメイドの変身処理
     *
     * @return
     */
    public boolean metamorphoseMaid(Entity targetEntity, PlayerEntity player, Hand hand) {

        //生き物であること
        if (!(targetEntity instanceof LivingEntity)) {
            return false;
        }

        ItemStack stack = player.getHeldItem(hand);

        LivingEntity living = (LivingEntity) targetEntity;
        World world = living.getEntityWorld();

        if (world.isRemote) return false;

        //カスタムNBTデータ取得
        CompoundNBT custamTag = living.getPersistentData();
        CompoundNBT animalMaidTag = null;
        ;
        if (custamTag.contains(ANIMAL_MAID_KEY)) {
            animalMaidTag = (CompoundNBT) custamTag.get(ANIMAL_MAID_KEY);
        }

        //メイドさんの動物変身
        if (animalMaidTag != null && living instanceof EntityLittleMaid) {

            //動物を生成
            LivingEntity animalEntity = (LivingEntity) EntityType.func_220335_a(animalMaidTag, world, (entity -> entity));

            //メイドさん情報を登録
            animalEntity = createMetamorphoseEntityLiving(living, animalEntity);

            //サーバーサイド
            //動物のスポーン
            animalEntity.setLocationAndAngles(living.getPosX(), living.getPosY(), living.getPosZ(),
                    0.0F, 0.0F);
            world.addEntity(animalEntity);
            //メイドさん消去
            living.remove();

            //ハートパーティクルと音設定
            //world.setEntityState(animalEntity, (byte)7);
            LMRNetwork.PacketSpawnParticleS2C(animalEntity.getPosition(), 0);
            ((EntityLittleMaid) living).playSound("entity.leashknot.place");

            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return true;

        }

        //動物のメイド変身
        //タグを持っている場合は無条件でアニマル形態と判断する
        if (animalMaidTag != null) {

            //メイドさんを生成する
            EntityLittleMaid littleMaid = (EntityLittleMaid) EntityType.func_220335_a(animalMaidTag, world, (entity -> entity));

            //動物情報を登録
            littleMaid = (EntityLittleMaid) createMetamorphoseEntityLiving(living, littleMaid);

            if (!world.isRemote) {
                //メイドさんのスポーン
                littleMaid.setLocationAndAngles(living.getPosX(), living.getPosY(), living.getPosZ(),
                        0.0F, 0.0F);
                world.addEntity(littleMaid);

                //メイドさん消去
                living.remove();

                //ハートパーティクルと音設定
                world.setEntityState(littleMaid, (byte) 7);
                littleMaid.playSound("entity.item.pickup");
            }

            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return true;
        }

        //テイム動物のメイド化
        if (isAnimalMaid(living, player)) {

            //メイドさんを生成する
            EntityLittleMaid littleMaid = Registration.LITTLE_MAID_MOB.get().create(world);

            //メイドさん初期契約
            littleMaid.setFirstContract(player);

            //動物情報を登録
            littleMaid = (EntityLittleMaid) createMetamorphoseEntityLiving(living, littleMaid);

            //メイドさんのスポーン
            littleMaid.setLocationAndAngles(living.getPosX(), living.getPosY(), living.getPosZ(),
                    0.0F, 0.0F);
            world.addEntity(littleMaid);

            //動物消去
            living.remove();

            //ハートパーティクルと音設定
            world.setEntityState(littleMaid, (byte) 7);
            littleMaid.playSound("entity.item.pickup");

            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return true;
        }

        return false;
    }

    /**
     * メイドさんの変身対象の動物か判断する
     * <p>
     * テイム系の動物 + ご主人様が一致する場合対象とする
     *
     * @return
     */
    public static boolean isAnimalMaid(LivingEntity living, PlayerEntity player) {

        //メイドさんは除外
        if (living instanceof EntityLittleMaid) return false;

        //テイム系Mob
        if (living instanceof TameableEntity) {

            TameableEntity tameableEntity = (TameableEntity) living;

            //ご主人様判定
            return player.getUniqueID().equals(tameableEntity.getOwnerId());
            //馬系Mob
        } else if (living instanceof AbstractHorseEntity) {

            AbstractHorseEntity targetEntity = (AbstractHorseEntity) living;
            //ご主人様判定
            return player.getUniqueID().equals(targetEntity.getOwnerUniqueId());
            //カスタムMob
        } else return LMRConfig.cfg_custom_animal_maid_mob_ids
                .contains(LittleMaidHelper.getEntityId(living));
    }

    /**
     * メイドさん -> 動物
     * 動物 -> メイドさん
     * 変換Entityを生成する
     *
     * @return
     */
    public static LivingEntity createMetamorphoseEntityLiving(LivingEntity fromEntity, LivingEntity toEntity) {

        //From側のカスタムデータを削除する
        fromEntity.getPersistentData().remove(LMItemMaidSugar.ANIMAL_MAID_KEY);

        //fromEntityをNBT化
        CompoundNBT animalTag = LittleMaidHelper.getNBTTagFromLivingEntity(fromEntity);

        //fromEntity情報の登録
        CompoundNBT customTag = toEntity.getPersistentData();
        customTag.put(ANIMAL_MAID_KEY, animalTag);

        //HPの同期
        double fromHpRatio = ((double) fromEntity.getHealth()) / fromEntity.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();
        double toHpMax = toEntity.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();

        toEntity.setHealth((float) Math.max(1.0D, toHpMax * fromHpRatio));

        return toEntity;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new StringTextComponent(TextFormatting.LIGHT_PURPLE + I18n.format("item.maid_sugar.info")));
    }

}
