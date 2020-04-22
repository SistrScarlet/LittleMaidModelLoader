package net.firis.lmt.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.blacklab.lmr.entity.littlemaid.EntityLittleMaid;
import net.blacklab.lmr.setup.ModSetup;
import net.firis.lmt.config.FirisConfig;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LMItemPlayerMaidBook extends Item {
	
	/**
	 * コンストラクタ
	 */
	public LMItemPlayerMaidBook() {
		super(new Item.Properties()
				.maxStackSize(1)
				.group(ModSetup.ITEM_GROUP));
	}

	/**
	 * 左クリックからのアイテム化
	 */
	@Override
	public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
		setDressUpPlayerFromMaid(player, entity);
		return true;
	}

	/**
	 * Shift＋右クリックからのアイテム化
	 */
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand)
    {
		setDressUpPlayerFromMaid(playerIn, target);
		return true;
    }
	
	/**
	 * プレイヤーがメイドさんの見た目になる
	 */
	public void setDressUpPlayerFromMaid(PlayerEntity player, Entity entity) {
		
		if (!player.world.isRemote) return;
		
		if (!(entity instanceof EntityLittleMaid)) return;
		
		//対象のメイドさんからモデル情報を取得する
		EntityLittleMaid entityMaid = (EntityLittleMaid) entity;
		
		//メイドモデル名取得
		String maidModelName = entityMaid.getTextureBox()[0].textureName;
		Integer maidModelColor = (int) entityMaid.getColor();
		String armorModelName = entityMaid.getTextureBox()[1].textureName;
		
		//メイドモデルの設定
		if (!player.isShiftKeyDown()) {
			
			//Config操作用
			Property propModel = FirisConfig.config.get(FirisConfig.CATEGORY_AVATAR, "01.MaidModel", FirisConfig.cfg_maid_model);
			Property propModelColor = FirisConfig.config.get(FirisConfig.CATEGORY_AVATAR, "02.MaidColorNo", FirisConfig.cfg_maid_color);

			//メイドモデルの指定
			propModel.set(maidModelName);
			propModelColor.set(maidModelColor);
			
		//アーマーモデルの設定
		} else {
			
			//Config操作用
			Property propModelArmorHelmet = FirisConfig.config.get(FirisConfig.CATEGORY_AVATAR, "03.ArmorHelmetModel", FirisConfig.cfg_armor_model_head);
			Property propModelArmorChest = FirisConfig.config.get(FirisConfig.CATEGORY_AVATAR, "04.ArmorChestplateModel", FirisConfig.cfg_armor_model_body);
			Property propModelArmorLegg = FirisConfig.config.get(FirisConfig.CATEGORY_AVATAR, "05.ArmorLeggingsModel", FirisConfig.cfg_armor_model_leg);
			Property propModelArmorBoots = FirisConfig.config.get(FirisConfig.CATEGORY_AVATAR, "06.ArmorBootsModel", FirisConfig.cfg_armor_model_boots);
			
			//スニーク中はアーマーモデルも設定
			if (player.getHeldItemOffhand().isEmpty()) {
				
				//全部のモデルを反映
				propModelArmorHelmet.set(armorModelName);
				propModelArmorChest.set(armorModelName);
				propModelArmorLegg.set(armorModelName);
				propModelArmorBoots.set(armorModelName);
				
			} else {
				ItemStack offHandStack = player.getHeldItemOffhand();
				//頭防具
				if (offHandStack.getItem().canEquip(offHandStack, EquipmentSlotType.HEAD, player)) {
					propModelArmorHelmet.set(armorModelName);
				}
				//胴防具
				if (offHandStack.getItem().canEquip(offHandStack, EquipmentSlotType.CHEST, player)) {
					propModelArmorChest.set(armorModelName);
				}
				//腰防具
				if (offHandStack.getItem().canEquip(offHandStack, EquipmentSlotType.LEGS, player)) {
					propModelArmorLegg.set(armorModelName);
				}
				//足防具
				if (offHandStack.getItem().canEquip(offHandStack, EquipmentSlotType.FEET, player)) {
					propModelArmorBoots.set(armorModelName);
				}
			}
		}
		
		//設定ファイル同期
		FirisConfig.syncConfig();
		
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new StringTextComponent(TextFormatting.LIGHT_PURPLE + I18n.format("item.player_maid_book.info")));
	}

}
