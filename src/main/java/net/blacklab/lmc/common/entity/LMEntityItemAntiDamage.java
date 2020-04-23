package net.blacklab.lmc.common.entity;

import net.blacklab.lmr.setup.Registration;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * 炎上と爆発に耐性を持つEntityItem
 *
 * @author computer
 */
//Registrationから不可燃に設定するようになった
public class LMEntityItemAntiDamage extends ItemEntity {

    public LMEntityItemAntiDamage(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
        this.lifespan = Integer.MAX_VALUE;
    }

    public LMEntityItemAntiDamage(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
        this.lifespan = Integer.MAX_VALUE;
    }

    public LMEntityItemAntiDamage(World worldIn) {
        super(Registration.ANTI_DAMAGE_ITEM_ENTITY.get(), worldIn);
        this.lifespan = Integer.MAX_VALUE;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        //voidダメージ以外は無効化する
        return source.getDamageType().equals(DamageSource.OUT_OF_WORLD.damageType);
    }

}
